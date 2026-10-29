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
import com.ilililissue.newsdraft.application.port.out.NewsSearchPort;
import com.ilililissue.newsdraft.config.NewsDraftProperties;
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

    @Mock
    NewsSearchPort newsSearchPort;

    @Mock
    IssueDraftWriterPort issueDraftWriterPort;

    @Mock
    IssueQuery issueQuery;

    @Mock
    ManageIssueUseCase manageIssueUseCase;

    private NewsDraftService service(NewsDraftProperties properties) {
        return new NewsDraftService(newsSearchPort, issueDraftWriterPort, issueQuery, manageIssueUseCase, properties, CLOCK);
    }

    private static NewsDraftProperties properties(String... keywords) {
        return new NewsDraftProperties(true, List.of(keywords), 30, 200, 3, "id", "secret", "key", null);
    }

    private static NewsArticle article(String url, OffsetDateTime publishedAt) {
        return new NewsArticle("제목", url, "요약문", publishedAt);
    }

    private void givenCategoryAndNoIssues() {
        when(issueQuery.getCategories()).thenReturn(List.of(Category.withId(new CategoryId(1), "사회")));
        when(issueQuery.getAllIssues(any(PageQuery.class))).thenReturn(new PageResult<>(List.of(), 0, 30, 0));
    }

    @DisplayName("지난 24시간 기사만 중복 없이 넘기고, 수집한 URL인 출처만 검색 결과 발행일로 등록한다")
    @Test
    void registersDraftWithCollectedSourcesOnly() {
        givenCategoryAndNoIssues();
        NewsArticle fresh = article("https://news.example.com/a", NOW.minusHours(3));
        NewsArticle other = article("https://other.example.com/b", NOW.minusHours(20).withOffsetSameInstant(KST));
        NewsArticle old = article("https://news.example.com/old", NOW.minusHours(30));
        when(newsSearchPort.searchLatest("경제", 30)).thenReturn(List.of(fresh, old));
        when(newsSearchPort.searchLatest("사회", 30)).thenReturn(List.of(fresh, other));
        when(issueDraftWriterPort.write(anyList(), anyList(), anyList(), anyInt())).thenReturn(List.of(
                new IssueDraft("질문인가?", "배경 설명", 1, List.of(
                        new IssueDraft.Source("https://news.example.com/a", "예시일보"),
                        new IssueDraft.Source("https://made-up.example.com/x", "가짜일보"),
                        new IssueDraft.Source("https://other.example.com/b", " ")))));
        when(manageIssueUseCase.create(any())).thenReturn(new IssueId(10));

        NewsDraftRun run = service(properties("경제", "사회")).generate();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<NewsArticle>> articles = ArgumentCaptor.forClass(List.class);
        verify(issueDraftWriterPort).write(articles.capture(), anyList(), anyList(), eq(3));
        Assertions.assertThat(articles.getValue()).containsExactly(fresh, other);

        ArgumentCaptor<IssueCommand> command = ArgumentCaptor.forClass(IssueCommand.class);
        verify(manageIssueUseCase).create(command.capture());
        Assertions.assertThat(command.getValue().sources().getValues())
                .extracting(s -> s.getUrl(), s -> s.getPublisher(), s -> s.getPublishedOn())
                .containsExactly(
                        Assertions.tuple("https://news.example.com/a", "예시일보", LocalDate.of(2026, 10, 29)),
                        Assertions.tuple("https://other.example.com/b", "other.example.com", LocalDate.of(2026, 10, 28)));
        Assertions.assertThat(run.articleCount()).isEqualTo(2);
        Assertions.assertThat(run.createdIssueIds()).containsExactly(10L);
        Assertions.assertThat(run.error()).isNull();
    }

    @DisplayName("없는 카테고리, 수집하지 않은 출처, 검증에 실패한 초안은 건너뛴다")
    @Test
    void skipsInvalidDrafts() {
        givenCategoryAndNoIssues();
        when(newsSearchPort.searchLatest("경제", 30)).thenReturn(List.of(article("https://news.example.com/a", NOW.minusHours(1))));
        when(issueDraftWriterPort.write(anyList(), anyList(), anyList(), anyInt())).thenReturn(List.of(
                new IssueDraft("질문인가?", "배경", 99, List.of(new IssueDraft.Source("https://news.example.com/a", "예시일보"))),
                new IssueDraft("질문인가?", "배경", 1, List.of(new IssueDraft.Source("https://made-up.example.com/x", "가짜일보"))),
                new IssueDraft("질".repeat(101), "배경", 1, List.of(new IssueDraft.Source("https://news.example.com/a", "예시일보")))));

        NewsDraftRun run = service(properties("경제")).generate();

        verify(manageIssueUseCase, never()).create(any());
        Assertions.assertThat(run.createdIssueIds()).isEmpty();
        Assertions.assertThat(run.skippedDrafts()).isEqualTo(3);
    }

    @DisplayName("API 키가 없으면 검색하지 않고 설정 오류로 끝나며, 마지막 실행 결과에 사유를 남긴다")
    @Test
    void requiresKeys() {
        NewsDraftService service = service(new NewsDraftProperties(false, List.of("경제"), 0, 0, 0, "", "", "", null));

        Assertions.assertThatThrownBy(service::generate).isInstanceOf(NewsDraftException.class);

        verify(newsSearchPort, never()).searchLatest(any(), anyInt());
        Assertions.assertThat(service.isRunning()).isFalse();
        Assertions.assertThat(service.lastRun()).get().extracting(NewsDraftRun::error).asString().contains("네이버");
    }

    @DisplayName("모든 검색어 검색이 실패하면 오류로 끝난다")
    @Test
    void failsWhenAllSearchesFail() {
        when(issueQuery.getCategories()).thenReturn(List.of(Category.withId(new CategoryId(1), "사회")));
        when(newsSearchPort.searchLatest(any(), anyInt())).thenThrow(new IllegalStateException("401"));

        Assertions.assertThatThrownBy(() -> service(properties("경제", "사회")).generate())
                .isInstanceOf(NewsDraftException.class);
        verify(issueDraftWriterPort, never()).write(anyList(), anyList(), anyList(), anyInt());
    }
}
