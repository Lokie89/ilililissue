package com.ilililissue.vote.application.service;

import com.ilililissue.issue.application.port.in.IssueQuery;
import com.ilililissue.issue.domain.entity.Issue;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.vote.application.VoteException;
import com.ilililissue.vote.application.port.in.VoteResult;
import com.ilililissue.vote.application.port.in.VoteUseCase;
import com.ilililissue.vote.application.port.out.VotePort;
import com.ilililissue.vote.domain.entity.Vote;
import com.ilililissue.vote.domain.vo.Stance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class VoteService implements VoteUseCase {

    private final IssueQuery issueQuery;
    private final VotePort votePort;
    private final Clock clock;

    @Transactional
    @Override
    public Vote cast(IssueId issueId, MemberId memberId, Stance stance) {
        Issue issue = issueQuery.getPublicIssue(issueId).issue();
        if (votePort.findByIssueIdAndMemberId(issueId, memberId).isPresent()) {
            throw VoteException.alreadyVoted();
        }
        return votePort.save(Vote.cast(issue, memberId, stance, LocalDateTime.now(clock)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Vote> findVote(IssueId issueId, MemberId memberId) {
        return votePort.findByIssueIdAndMemberId(issueId, memberId);
    }

    @Transactional(readOnly = true)
    @Override
    public VoteResult getResult(IssueId issueId, MemberId viewerId) {
        issueQuery.getPublicIssue(issueId);
        requireVote(issueId, viewerId);
        return VoteResult.from(votePort.countByStance(issueId));
    }

    @Transactional(readOnly = true)
    @Override
    public Vote requireVote(IssueId issueId, MemberId memberId) {
        return votePort.findByIssueIdAndMemberId(issueId, memberId).orElseThrow(VoteException::voteRequired);
    }
}
