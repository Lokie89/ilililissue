package com.ilililissue.vote.application.port.in;

import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.vote.domain.entity.Vote;
import com.ilililissue.vote.domain.vo.Stance;

import java.util.Optional;

public interface VoteUseCase {

    Vote cast(IssueId issueId, MemberId memberId, Stance stance);

    Optional<Vote> findVote(IssueId issueId, MemberId memberId);

    /**
     * 투표한 회원에게만 결과를 공개한다.
     */
    VoteResult getResult(IssueId issueId, MemberId viewerId);

    /**
     * 투표하지 않은 회원이면 예외를 던진다.
     */
    Vote requireVote(IssueId issueId, MemberId memberId);
}
