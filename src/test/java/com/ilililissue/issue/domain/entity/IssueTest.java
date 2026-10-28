package com.ilililissue.issue.domain.entity;

import com.ilililissue.issue.domain.IllegalIssueStateException;
import com.ilililissue.issue.domain.IssueFixture;
import com.ilililissue.issue.domain.vo.CategoryId;
import com.ilililissue.issue.domain.vo.IssueSources;
import com.ilililissue.issue.domain.vo.IssueStatus;
import com.ilililissue.issue.domain.vo.IssueSummary;
import com.ilililissue.issue.domain.vo.IssueTitle;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.ilililissue.issue.domain.IssueFixture.NOW;

public class IssueTest {

    @DisplayName("이슈는 작성 → 검수 → 게시 → 마감 순서로 진행된다")
    @Test
    void lifecycle() {
        Issue issue = IssueFixture.draft(IssueFixture.sources());
        Assertions.assertThat(issue.getStatus()).isEqualTo(IssueStatus.DRAFT);
        Assertions.assertThat(issue.isOpenForVoting()).isFalse();

        issue.review();
        issue.publish(NOW);
        Assertions.assertThat(issue.getStatus()).isEqualTo(IssueStatus.PUBLISHED);
        Assertions.assertThat(issue.getPublishedAt()).isEqualTo(NOW);
        Assertions.assertThat(issue.isOpenForVoting()).isTrue();

        issue.close();
        Assertions.assertThat(issue.isOpenForVoting()).isFalse();
    }

    @DisplayName("검수하지 않은 이슈는 게시할 수 없다")
    @Test
    void publishWithoutReview() {
        Issue issue = IssueFixture.draft(IssueFixture.sources());

        Assertions.assertThatThrownBy(() -> issue.publish(NOW))
                .isInstanceOf(IllegalIssueStateException.class);
    }

    @DisplayName("출처가 없는 이슈는 게시할 수 없다")
    @Test
    void publishWithoutSources() {
        Issue issue = IssueFixture.draft(IssueSources.empty());
        issue.review();

        Assertions.assertThatThrownBy(() -> issue.publish(NOW))
                .isInstanceOf(IllegalIssueStateException.class)
                .hasMessageContaining("출처");
    }

    @DisplayName("검수된 이슈를 수정하면 다시 검수가 필요하다")
    @Test
    void editAfterReview() {
        Issue issue = IssueFixture.draft(IssueFixture.sources());
        issue.review();

        issue.edit(IssueTitle.from("수정된 질문"), IssueSummary.from("수정된 설명"), new CategoryId(2),
                IssueFixture.sources());

        Assertions.assertThat(issue.getStatus()).isEqualTo(IssueStatus.DRAFT);
        Assertions.assertThat(issue.getTitle()).isEqualTo(IssueTitle.from("수정된 질문"));
    }

    @DisplayName("게시된 이슈는 내용을 수정할 수 없고 배경 설명 정정만 할 수 있다")
    @Test
    void correctAfterPublish() {
        Issue issue = IssueFixture.withStatus(1, IssueStatus.PUBLISHED);

        Assertions.assertThatThrownBy(() -> issue.edit(IssueTitle.from("수정"), IssueSummary.from("수정"),
                new CategoryId(1), IssueFixture.sources())).isInstanceOf(IllegalIssueStateException.class);

        Assertions.assertThat(issue.isCorrected()).isFalse();
        issue.correct(IssueSummary.from("정정된 설명"), NOW.plusHours(1));

        Assertions.assertThat(issue.isCorrected()).isTrue();
        Assertions.assertThat(issue.getCorrectedAt()).isEqualTo(NOW.plusHours(1));
        Assertions.assertThat(issue.getSummary()).isEqualTo(IssueSummary.from("정정된 설명"));
    }

    @DisplayName("게시 전 이슈는 정정할 수 없다")
    @Test
    void correctBeforePublish() {
        Issue issue = IssueFixture.draft(IssueFixture.sources());

        Assertions.assertThatThrownBy(() -> issue.correct(IssueSummary.from("정정"), NOW))
                .isInstanceOf(IllegalIssueStateException.class);
    }

    @DisplayName("빈 제목, 너무 긴 제목은 허용하지 않는다")
    @Test
    void invalidTitle() {
        Assertions.assertThatThrownBy(() -> IssueTitle.from(" "));
        Assertions.assertThatThrownBy(() -> IssueTitle.from("가".repeat(101)));
    }

    @DisplayName("이슈 동일성은 id로 판단한다")
    @Test
    void equals() {
        Assertions.assertThat(IssueFixture.withStatus(1, IssueStatus.DRAFT))
                .isEqualTo(IssueFixture.withStatus(1, IssueStatus.PUBLISHED));
        Assertions.assertThat(IssueFixture.withStatus(1, IssueStatus.DRAFT))
                .isNotEqualTo(IssueFixture.withStatus(2, IssueStatus.DRAFT));
    }
}
