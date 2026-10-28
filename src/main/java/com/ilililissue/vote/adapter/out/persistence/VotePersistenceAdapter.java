package com.ilililissue.vote.adapter.out.persistence;

import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.vote.application.port.out.VotePort;
import com.ilililissue.vote.domain.entity.Vote;
import com.ilililissue.vote.domain.vo.Stance;
import com.ilililissue.vote.domain.vo.VoteId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class VotePersistenceAdapter implements VotePort {

    private final VoteJpaRepository voteJpaRepository;

    @Override
    public Vote save(Vote vote) {
        return mapToDomain(voteJpaRepository.save(new VoteJpaEntity(
                vote.getId() == null ? null : vote.getId().getValue(),
                vote.getIssueId().getValue(),
                vote.getMemberId().getValue(),
                vote.getStance(),
                vote.getVotedAt()
        )));
    }

    @Override
    public Optional<Vote> findByIssueIdAndMemberId(IssueId issueId, MemberId memberId) {
        return voteJpaRepository.findByIssueIdAndMemberId(issueId.getValue(), memberId.getValue())
                .map(this::mapToDomain);
    }

    @Override
    public Map<Stance, Long> countByStance(IssueId issueId) {
        Map<Stance, Long> counts = new EnumMap<>(Stance.class);
        voteJpaRepository.countByStance(issueId.getValue())
                .forEach(row -> counts.put(row.getStance(), row.getCount()));
        return counts;
    }

    private Vote mapToDomain(VoteJpaEntity entity) {
        return Vote.withId(
                new VoteId(entity.getId()),
                new IssueId(entity.getIssueId()),
                new MemberId(entity.getMemberId()),
                entity.getStance(),
                entity.getVotedAt()
        );
    }
}
