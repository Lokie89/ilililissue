package com.ilililissue.newsdraft.application.service;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.issue.application.port.in.IssueCommand;
import com.ilililissue.issue.application.port.in.IssueQuery;
import com.ilililissue.issue.application.port.in.ManageIssueUseCase;
import com.ilililissue.issue.domain.entity.Category;
import com.ilililissue.issue.domain.vo.CategoryId;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.newsdraft.application.NewsDraftException;
import com.ilililissue.newsdraft.application.port.in.GenerateNewsDraftsUseCase.NewsDraftRun;
import com.ilililissue.newsdraft.application.port.out.IssueDraftWriterPort;
import com.ilililissue.newsdraft.application.port.out.NewsFeedPort;
import com.ilililissue.newsdraft.config.NewsDraftProperties;
import com.ilililissue.newsdraft.config.NewsDraftProperties.Feed;
import com.ilililissue.newsdraft.domain.IssueDraft;
import com.ilililissue.newsdraft.domain.NewsArticle;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsDraftServiceTest {

    private static final ZoneOffset KST = ZoneOffset.ofHours(9);
    // 2026-10-29 07:00 KST
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-28T22:00:00Z"), ZoneId.of("Asia/Seoul"));
    private static final OffsetDateTime NOW = OffsetDateTime.now(CLOCK);
    private static final Feed YNA = new Feed("연합뉴스", "사회", "https://yna.example.com/rss");
    private static final Feed HANI = new Feed("한겨레", "사회", "https://hani.example.com/rss");

    @Mock
    NewsFeedPort newsFeedPort;

    @Mock
    IssueDraftWriterPort issueDraftWriterPort;

    @Mock
    IssueQuery issueQuery;

    @Mock
    ManageIssueUseCase manageIssueUseCase;

    private NewsDraftService service(NewsDraftProperties properties) {
        return new NewsDraftService(newsFeedPort, issueDraftWriterPort, issueQuery, manageIssueUseCase, properties, CLOCK);
    }

    private static NewsDraftProperties properties(Feed... feeds) {
        return new NewsDraftProperties(true, List.of(feeds), 40, 200, 3, "key", null, null, null, null);
    }

    private static NewsArticle article(String publisher, String url, OffsetDateTime publishedAt) {
        return new NewsArticle(publisher, "사회", "제목", url, "요약문", publishedAt);
    }

    private void givenCategoryAndNoIssues() {
        when(issueQuery.getCategories()).thenReturn(List.of(Category.withId(new CategoryId(1), "사회")));
        when(issueQuery.getAllIssues(any(PageQuery.class))).thenReturn(new PageResult<>(List.of(), 0, 30, 0));
    }

    @DisplayName("지난 24시간 기사만 중복 없이 넘기고, 수집한 URL인 출처만 피드의 언론사·발행일로 등록한다")
    @Test
    void registersDraftWithCollectedSourcesOnly() {
        givenCategoryAndNoIssues();
        NewsArticle fresh = article("연합뉴스", "https://yna.example.com/a", NOW.minusHours(3));
        NewsArticle other = article("한겨레", "https://hani.example.com/b", NOW.minusHours(20).withOffsetSameInstant(KST));
        NewsArticle old = article("연합뉴스", "https://yna.example.com/old", NOW.minusHours(30));
        when(newsFeedPort.fetch(YNA)).thenReturn(List.of(fresh, old));
        when(newsFeedPort.fetch(HANI)).thenReturn(List.of(other, article("한겨레", "https://yna.example.com/a", NOW)));
        when(issueDraftWriterPort.write(anyList(), anyList(), anyList(), anyInt())).thenReturn(List.of(
                new IssueDraft("질문인가?", "배경 설명", 1, List.of(
                        "https://yna.example.com/a",
                        "https://made-up.example.com/x",
                        "https://hani.example.com/b",
                        "https://yna.example.com/a"))));
        when(manageIssueUseCase.create(any())).thenReturn(new IssueId(10));

        NewsDraftRun run = service(properties(YNA, HANI)).generate();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<NewsArticle>> articles = ArgumentCaptor.forClass(List.class);
        verify(issueDraftWriterPort).write(articles.capture(), anyList(), anyList(), eq(3));
        Assertions.assertThat(articles.getValue()).containsExactly(other, fresh);

        ArgumentCaptor<IssueCommand> command = ArgumentCaptor.forClass(IssueCommand.class);
        verify(manageIssueUseCase).create(command.capture());
        Assertions.assertThat(command.getValue().sources().getValues())
                .extracting(s -> s.getUrl(), s -> s.getPublisher(), s -> s.getPublishedOn())
                .containsExactly(
                        Assertions.tuple("https://yna.example.com/a", "연합뉴스", LocalDate.of(2026, 10, 29)),
                        Assertions.tuple("https://hani.example.com/b", "한겨레", LocalDate.of(2026, 10, 28)));
        Assertions.assertThat(run.articleCount()).isEqualTo(2);
        Assertions.assertThat(run.createdIssueIds()).containsExactly(10L);
        Assertions.assertThat(run.error()).isNull();
    }

    @DisplayName("상한을 넘으면 최신순으로 자르지 않고 처음부터 끝까지 고르게 뽑는다")
    @Test
    void evenlySpaced() {
        List<Integer> hours = java.util.stream.IntStream.range(0, 24).boxed().toList();

        Assertions.assertThat(NewsDraftService.evenlySpaced(hours, 4)).containsExactly(0, 8, 15, 23);
        Assertions.assertThat(NewsDraftService.evenlySpaced(hours, 30)).isEqualTo(hours);
        Assertions.assertThat(NewsDraftService.evenlySpaced(hours, 1)).containsExactly(23);
    }

    @DisplayName("없는 카테고리, 수집하지 않은 출처, 검증에 실패한 초안은 건너뛴다")
    @Test
    void skipsInvalidDrafts() {
        givenCategoryAndNoIssues();
        when(newsFeedPort.fetch(YNA)).thenReturn(List.of(article("연합뉴스", "https://yna.example.com/a", NOW.minusHours(1))));
        when(issueDraftWriterPort.write(anyList(), anyList(), anyList(), anyInt())).thenReturn(List.of(
                new IssueDraft("질문인가?", "배경", 99, List.of("https://yna.example.com/a")),
                new IssueDraft("질문인가?", "배경", 1, List.of("https://made-up.example.com/x")),
                new IssueDraft("질".repeat(101), "배경", 1, List.of("https://yna.example.com/a"))));

        NewsDraftRun run = service(properties(YNA)).generate();

        verify(manageIssueUseCase, never()).create(any());
        Assertions.assertThat(run.createdIssueIds()).isEmpty();
        Assertions.assertThat(run.skippedDrafts()).isEqualTo(3);
    }

    @DisplayName("API 키가 없으면 피드를 읽지 않고 설정 오류로 끝나며, 마지막 실행 결과에 사유를 남긴다")
    @Test
    void requiresKey() {
        NewsDraftService service = service(new NewsDraftProperties(false, List.of(YNA), 0, 0, 0, "", null, null, null, null));

        Assertions.assertThatThrownBy(service::generate).isInstanceOf(NewsDraftException.class);

        verify(newsFeedPort, never()).fetch(any());
        Assertions.assertThat(service.isRunning()).isFalse();
        Assertions.assertThat(service.lastRun()).get().extracting(NewsDraftRun::error).asString().contains("OPENAI_API_KEY");
    }

    @DisplayName("Codex CLI 방식은 OpenAI API 키 없이 실행한다")
    @Test
    void codexCliNeedsNoKey() {
        givenCategoryAndNoIssues();
        when(newsFeedPort.fetch(YNA)).thenReturn(List.of(article("연합뉴스", "https://yna.example.com/a", NOW.minusHours(1))));
        when(issueDraftWriterPort.write(anyList(), anyList(), anyList(), anyInt())).thenReturn(List.of());

        NewsDraftRun run = service(new NewsDraftProperties(false, List.of(YNA), 0, 0, 0, "", null,
                NewsDraftProperties.WRITER_CODEX_CLI, null, null)).generate();

        Assertions.assertThat(run.error()).isNull();
        Assertions.assertThat(run.articleCount()).isEqualTo(1);
    }

    @DisplayName("피드를 하나도 읽지 못하면 초안을 만들지 않고 오류로 끝난다")
    @Test
    void feedFailures() {
        when(issueQuery.getCategories()).thenReturn(List.of(Category.withId(new CategoryId(1), "사회")));
        when(newsFeedPort.fetch(any())).thenThrow(new IllegalStateException("403"));

        Assertions.assertThatThrownBy(() -> service(properties(YNA, HANI)).generate())
                .isInstanceOf(NewsDraftException.class);
        verify(issueDraftWriterPort, never()).write(anyList(), anyList(), anyList(), anyInt());
    }
}
