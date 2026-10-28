package com.ilililissue.opinion.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface RecommendationJpaRepository extends JpaRepository<RecommendationJpaEntity, Long> {

    boolean existsByOpinionIdAndMemberId(Long opinionId, Long memberId);

    @Query("select r.opinionId from RecommendationJpaEntity r "
            + "where r.memberId = :memberId and r.opinionId in :opinionIds")
    List<Long> findRecommendedOpinionIds(Long memberId, Collection<Long> opinionIds);
}
