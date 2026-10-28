package com.ilililissue.vote.domain.entity;

import com.ilililissue.issue.domain.IssueFixture;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.issue.domain.vo.IssueStatus;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.vote.domain.IssueNotOpenForVotingException;
import com.ilililissue.vote.domain.vo.Stance;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.ilililissue.issue.domain.IssueFixture.NOW;

public class VoteTest {

    private final MemberId memberId = new MemberId(10);

    @DisplayName("게시된 이슈에 입장을 선택해 투표한다")
    @Test
    void cast() {
        Vote vote = Vote.cast(IssueFixture.withStatus(1, IssueStatus.PUBLISHED), memberId, Stance.DISAGREE, NOW);

        Assertions.assertThat(vote.getIssueId()).isEqualTo(new IssueId(1));
        Assertions.assertThat(vote.getMemberId()).isEqualTo(memberId);
        Assertions.assertThat(vote.getStance()).isEqualTo(Stance.DISAGREE);
    }

    @DisplayName("게시 상태가 아닌 이슈에는 투표할 수 없다")
    @ParameterizedTest
    @EnumSource(value = IssueStatus.class, names = "PUBLISHED", mode = EnumSource.Mode.EXCLUDE)
    void castOnClosedIssue(IssueStatus status) {
        Assertions.assertThatThrownBy(
                        () -> Vote.cast(IssueFixture.withStatus(1, status), memberId, Stance.AGREE, NOW))
                .isInstanceOf(IssueNotOpenForVotingException.class);
    }

    @DisplayName("입장 없이 투표할 수 없다")
    @Test
    void castWithoutStance() {
        Assertions.assertThatThrownBy(
                () -> Vote.cast(IssueFixture.withStatus(1, IssueStatus.PUBLISHED), memberId, null, NOW));
    }
}
