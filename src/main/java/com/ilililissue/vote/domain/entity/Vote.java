package com.ilililissue.vote.domain.entity;

import com.ilililissue.common.SelfValidating;
import com.ilililissue.issue.domain.entity.Issue;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.vote.domain.IssueNotOpenForVotingException;
import com.ilililissue.vote.domain.vo.Stance;
import com.ilililissue.vote.domain.vo.VoteId;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * <h2>투표 도메인</h2>
 * <ul>
 * <li>게시(PUBLISHED)된 이슈에만 투표할 수 있다.</li>
 * <li>회원당 이슈별 1회이며 저장소의 (issueId, memberId) 유니크 제약으로 보장한다.</li>
 * <li>MVP에서는 입장을 변경하거나 취소할 수 없다.</li>
 * </ul>
 */
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
public class Vote extends SelfValidating<Vote> {

    private final VoteId id;

    @NotNull
    private final IssueId issueId;

    @NotNull
    private final MemberId memberId;

    @NotNull
    private final Stance stance;

    @NotNull
    private final LocalDateTime votedAt;

    private Vote(
            VoteId id,
            IssueId issueId,
            MemberId memberId,
            Stance stance,
            LocalDateTime votedAt
    ) {
        this.id = id;
        this.issueId = issueId;
        this.memberId = memberId;
        this.stance = stance;
        this.votedAt = votedAt;
        this.validate();
    }

    public static Vote cast(Issue issue, MemberId memberId, Stance stance, LocalDateTime now) {
        if (!issue.isOpenForVoting()) {
            throw new IssueNotOpenForVotingException();
        }
        return new Vote(null, issue.getId(), memberId, stance, now);
    }

    public static Vote withId(VoteId id, IssueId issueId, MemberId memberId, Stance stance, LocalDateTime votedAt) {
        return new Vote(id, issueId, memberId, stance, votedAt);
    }
}
