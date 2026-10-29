package com.ilililissue.newsdraft.application.service;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.issue.application.port.in.IssueCommand;
import com.ilililissue.issue.application.port.in.IssueQuery;
import com.ilililissue.issue.application.port.in.ManageIssueUseCase;
import com.ilililissue.issue.domain.entity.Category;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.newsdraft.application.NewsDraftException;
import com.ilililissue.newsdraft.application.port.in.GenerateIssueImageUseCase;
import com.ilililissue.newsdraft.application.port.in.GenerateNewsDraftsUseCase;
import com.ilililissue.newsdraft.application.port.out.IssueDraftWriterPort;
import com.ilililissue.newsdraft.application.port.out.NewsFeedPort;
import com.ilililissue.newsdraft.config.NewsDraftProperties;
import com.ilililissue.newsdraft.config.NewsDraftProperties.Feed;
import com.ilililissue.newsdraft.domain.IssueDraft;
import com.ilililissue.newsdraft.domain.NewsArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * <h2>뉴스 자동 초안</h2>
 * <ul>
 * <li>언론사 면별 RSS 피드에서 지난 24시간에 나온 기사를 모아 URL 기준으로 중복을 없앤다.
 * 피드마다 상한을 넘으면 최신순으로 자르지 않고 24시간에 고르게 뽑아, 밤사이 기사나 기사가 많은 언론사로 쏠리지 않게 한다.</li>
 * <li>RSS에는 조회수가 없어 '인기'는 보도량(같은 주제를 다룬 언론사 수)으로 판단하도록 AI에 맡긴다.</li>
 * <li>분야(카테고리)마다 상한({@code draftsPerCategory}, 기본 1)까지만 등록한다.</li>
 * <li>이미지를 만들 수 있는 설정(Codex CLI)이면 등록한 초안마다 대표 이미지를 만든다. 실패해도 초안은 남긴다.</li>
 * <li>AI가 고른 초안 중 카테고리가 있고 출처가 수집한 기사 URL인 것만 DRAFT 이슈로 등록한다. 언론사·발행일은 피드 값을 쓴다.</li>
 * <li>기사 제목·요약문은 저장하지 않는다. 게시는 관리자 검수 후에만 한다.</li>
 * <li>외부 호출이 길어 전체를 한 트랜잭션으로 묶지 않고, 초안마다 따로 등록한다. 서버 한 대 전제로 메모리에서 중복 실행을 막는다.</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class NewsDraftService implements GenerateNewsDraftsUseCase {

    private static final Duration COLLECT_WINDOW = Duration.ofHours(24);
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final int RECENT_ISSUE_COUNT = 30;
    private static final int PUBLISHER_MAX_LENGTH = 50;

    private final NewsFeedPort newsFeedPort;
    private final IssueDraftWriterPort issueDraftWriterPort;
    private final IssueQuery issueQuery;
    private final ManageIssueUseCase manageIssueUseCase;
    private final GenerateIssueImageUseCase generateIssueImageUseCase;
    private final NewsDraftProperties properties;
    private final Clock clock;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile NewsDraftRun lastRun;

    @Override
    public NewsDraftRun generate() {
        if (!running.compareAndSet(false, true)) {
            throw NewsDraftException.alreadyRunning();
        }
        LocalDateTime startedAt = LocalDateTime.now(clock);
        try {
            lastRun = run(startedAt);
            return lastRun;
        } catch (RuntimeException e) {
            lastRun = new NewsDraftRun(startedAt, LocalDateTime.now(clock), 0, List.of(), 0, 0, e.getMessage());
            throw e;
        } finally {
            running.set(false);
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public Optional<NewsDraftRun> lastRun() {
        return Optional.ofNullable(lastRun);
    }

    private NewsDraftRun run(LocalDateTime startedAt) {
        requireConfigured();
        List<Category> categories = issueQuery.getCategories();
        if (categories.isEmpty()) {
            throw NewsDraftException.notConfigured("카테고리가 없어 초안을 만들 수 없습니다. 관리 화면에서 카테고리를 먼저 추가하세요.");
        }

        List<NewsArticle> articles = collect();
        if (articles.isEmpty()) {
            log.info("뉴스 초안: 지난 24시간 기사가 없어 건너뜁니다.");
            return new NewsDraftRun(startedAt, LocalDateTime.now(clock), 0, List.of(), 0, 0, null);
        }

        List<String> recentTitles = issueQuery.getAllIssues(PageQuery.of(0, RECENT_ISSUE_COUNT)).content().stream()
                .map(view -> view.issue().getTitle().getValue())
                .toList();
        List<IssueDraft> drafts = issueDraftWriterPort.write(articles, categories, recentTitles, properties.draftsPerCategory());

        Map<String, NewsArticle> articlesByUrl = articles.stream()
                .collect(Collectors.toMap(NewsArticle::url, a -> a, (a, b) -> a));
        Set<Long> categoryIds = categories.stream().map(c -> c.getId().getValue()).collect(Collectors.toSet());
        List<Long> created = new ArrayList<>();
        int skipped = 0;
        Map<Long, Integer> createdPerCategory = new HashMap<>();
        for (IssueDraft draft : drafts) {
            if (createdPerCategory.getOrDefault(draft.categoryId(), 0) >= properties.draftsPerCategory()) {
                log.warn("뉴스 초안 제외: 분야별 상한 초과 - {}", draft.title());
                skipped++;
                continue;
            }
            Optional<Long> issueId = register(draft, articlesByUrl, categoryIds);
            issueId.ifPresent(id -> createdPerCategory.merge(draft.categoryId(), 1, Integer::sum));
            if (issueId.isPresent()) {
                created.add(issueId.get());
            } else {
                skipped++;
            }
        }
        log.info("뉴스 초안: 기사 {}건으로 초안 {}건 등록, {}건 제외", articles.size(), created.size(), skipped);
        int imageCount = attachImages(created);
        return new NewsDraftRun(startedAt, LocalDateTime.now(clock), articles.size(), created, skipped, imageCount, null);
    }

    // 한 장에 1~2분 걸려 하나씩 만든다. 실패해도 초안은 그대로 두고 관리 화면에서 다시 만들 수 있다.
    private int attachImages(List<Long> issueIds) {
        if (!generateIssueImageUseCase.available()) {
            return 0;
        }
        int count = 0;
        for (Long issueId : issueIds) {
            if (generateIssueImageUseCase.generate(new IssueId(issueId))) {
                count++;
            }
        }
        return count;
    }

    private void requireConfigured() {
        if (properties.usesOpenaiApi() && !properties.hasOpenaiKey()) {
            throw NewsDraftException.notConfigured("OpenAI API 키(OPENAI_API_KEY)가 설정되지 않았습니다.");
        }
        if (properties.feeds().isEmpty()) {
            throw NewsDraftException.notConfigured("언론사 RSS 피드(news-draft.feeds)가 없습니다.");
        }
    }

    private List<NewsArticle> collect() {
        OffsetDateTime since = OffsetDateTime.now(clock).minus(COLLECT_WINDOW);
        Map<String, NewsArticle> byUrl = new LinkedHashMap<>();
        int failedFeeds = 0;
        for (Feed feed : properties.feeds()) {
            try {
                List<NewsArticle> recent = newsFeedPort.fetch(feed).stream()
                        .filter(article -> article.publishedAt() != null && article.publishedAt().isAfter(since))
                        .sorted(Comparator.comparing(NewsArticle::publishedAt))
                        .toList();
                evenlySpaced(recent, properties.articlesPerFeed())
                        .forEach(article -> byUrl.putIfAbsent(article.url(), article));
            } catch (RuntimeException e) {
                failedFeeds++;
                log.warn("뉴스 초안: {} 피드 읽기 실패 - {}", feed.publisher(), e.getMessage());
            }
        }
        if (failedFeeds == properties.feeds().size()) {
            throw NewsDraftException.externalFailure("언론사 피드를 하나도 읽지 못했습니다. 네트워크와 피드 주소를 확인하세요.");
        }
        List<NewsArticle> collected = byUrl.values().stream()
                .sorted(Comparator.comparing(NewsArticle::publishedAt))
                .toList();
        return evenlySpaced(collected, properties.maxArticles());
    }

    /**
     * 시간순 목록에서 최대 limit개를 같은 간격으로 고른다(처음과 끝 포함).
     */
    static <T> List<T> evenlySpaced(List<T> items, int limit) {
        if (items.size() <= limit) {
            return items;
        }
        if (limit <= 1) {
            return items.subList(items.size() - 1, items.size());
        }
        List<T> picked = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            picked.add(items.get((int) Math.round((double) i * (items.size() - 1) / (limit - 1))));
        }
        return picked;
    }

    private Optional<Long> register(IssueDraft draft, Map<String, NewsArticle> articlesByUrl, Set<Long> categoryIds) {
        if (!categoryIds.contains(draft.categoryId())) {
            log.warn("뉴스 초안 제외: 없는 카테고리 {} - {}", draft.categoryId(), draft.title());
            return Optional.empty();
        }
        // AI가 만든 URL은 믿지 않고, 수집한 기사 URL과 그 언론사·발행일만 출처로 쓴다.
        List<IssueCommand.Source> sources = (draft.sourceUrls() == null ? List.<String>of() : draft.sourceUrls()).stream()
                .filter(articlesByUrl::containsKey)
                .distinct()
                .map(articlesByUrl::get)
                .map(article -> new IssueCommand.Source(article.url(), publisherOf(article),
                        article.publishedAt().atZoneSameInstant(SEOUL).toLocalDate()))
                .toList();
        if (sources.isEmpty()) {
            log.warn("뉴스 초안 제외: 수집한 기사와 맞는 출처가 없음 - {}", draft.title());
            return Optional.empty();
        }
        try {
            return Optional.of(manageIssueUseCase.create(
                    IssueCommand.of(draft.title(), draft.summary(), draft.categoryId(), sources,
                            draft.keyPoint(), draft.agreeReason(), draft.disagreeReason())).getValue());
        } catch (RuntimeException e) {
            log.warn("뉴스 초안 제외: 등록 실패 - {} ({})", draft.title(), e.getMessage());
            return Optional.empty();
        }
    }

    private static String publisherOf(NewsArticle article) {
        String publisher = article.publisher() == null ? "" : article.publisher().strip();
        if (publisher.isEmpty()) {
            publisher = "알 수 없음";
        }
        return publisher.length() > PUBLISHER_MAX_LENGTH ? publisher.substring(0, PUBLISHER_MAX_LENGTH) : publisher;
    }
}
