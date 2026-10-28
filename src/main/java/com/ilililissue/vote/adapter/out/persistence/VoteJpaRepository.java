package com.ilililissue.vote.adapter.out.persistence;

import com.ilililissue.vote.domain.vo.Stance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VoteJpaRepository extends JpaRepository<VoteJpaEntity, Long> {

    Optional<VoteJpaEntity> findByIssueIdAndMemberId(Long issueId, Long memberId);

    @Query("select v.stance as stance, count(v) as count from VoteJpaEntity v "
            + "where v.issueId = :issueId group by v.stance")
    List<StanceCount> countByStance(Long issueId);

    interface StanceCount {

        Stance getStance();

        long getCount();
    }
}
