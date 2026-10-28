package com.ilililissue.opinion.domain.entity;

import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.opinion.domain.AlreadyEditedOpinionException;
import com.ilililissue.opinion.domain.NotOpinionAuthorException;
import com.ilililissue.opinion.domain.vo.OpinionContent;
import com.ilililissue.vote.domain.entity.Vote;
import com.ilililissue.vote.domain.vo.Stance;
import com.ilililissue.vote.domain.vo.VoteId;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class OpinionTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 28, 9, 0);

    private final MemberId authorId = new MemberId(10);
    private final Vote vote = Vote.withId(new VoteId(1), new IssueId(1), authorId, Stance.NEUTRAL, NOW);

    @DisplayName("의견은 투표의 이슈, 작성자, 입장을 따른다")
    @Test
    void write() {
        Opinion opinion = Opinion.write(vote, OpinionContent.from("상황에 따라 다르다고 생각합니다."), NOW);

        Assertions.assertThat(opinion.getIssueId()).isEqualTo(vote.getIssueId());
        Assertions.assertThat(opinion.getAuthorId()).isEqualTo(authorId);
        Assertions.assertThat(opinion.getStance()).isEqualTo(Stance.NEUTRAL);
        Assertions.assertThat(opinion.isEdited()).isFalse();
        Assertions.assertThat(opinion.isHidden()).isFalse();
    }

    @DisplayName("작성자는 의견을 1회 수정할 수 있다")
    @Test
    void editOnce() {
        Opinion opinion = Opinion.write(vote, OpinionContent.from("원래 의견"), NOW);

        opinion.edit(authorId, OpinionContent.from("수정한 의견"));

        Assertions.assertThat(opinion.getContent()).isEqualTo(OpinionContent.from("수정한 의견"));
        Assertions.assertThat(opinion.isEdited()).isTrue();
    }

    @DisplayName("이미 수정한 의견은 다시 수정할 수 없다")
    @Test
    void editTwice() {
        Opinion opinion = Opinion.write(vote, OpinionContent.from("원래 의견"), NOW);
        opinion.edit(authorId, OpinionContent.from("수정한 의견"));

        Assertions.assertThatThrownBy(() -> opinion.edit(authorId, OpinionContent.from("또 수정")))
                .isInstanceOf(AlreadyEditedOpinionException.class)
                .hasMessageContaining("이미 수정된 의견");
    }

    @DisplayName("작성자가 아니면 수정할 수 없고 수정 기회도 차감되지 않는다")
    @Test
    void editByOther() {
        Opinion opinion = Opinion.write(vote, OpinionContent.from("원래 의견"), NOW);

        Assertions.assertThatThrownBy(() -> opinion.edit(new MemberId(99), OpinionContent.from("남의 수정")))
                .isInstanceOf(NotOpinionAuthorException.class);
        Assertions.assertThat(opinion.isEdited()).isFalse();
    }

    @DisplayName("빈 의견, 500자를 넘는 의견은 허용하지 않는다")
    @Test
    void invalidContent() {
        Assertions.assertThatThrownBy(() -> OpinionContent.from(" "));
        Assertions.assertThatThrownBy(() -> OpinionContent.from("가".repeat(501)));
        Assertions.assertThat(OpinionContent.from("가".repeat(500)).getValue()).hasSize(500);
    }

    @DisplayName("숨김 처리된 의견은 숨김 상태가 된다")
    @Test
    void hide() {
        Opinion opinion = Opinion.write(vote, OpinionContent.from("원래 의견"), NOW);

        opinion.hide();

        Assertions.assertThat(opinion.isHidden()).isTrue();
    }
}
