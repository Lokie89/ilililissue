package com.ilililissue.vote.application.port.out;

import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.vote.domain.entity.Vote;
import com.ilililissue.vote.domain.vo.Stance;

import java.util.Map;
import java.util.Optional;

public interface VotePort {

    Vote save(Vote vote);

    Optional<Vote> findByIssueIdAndMemberId(IssueId issueId, MemberId memberId);

    Map<Stance, Long> countByStance(IssueId issueId);
}
