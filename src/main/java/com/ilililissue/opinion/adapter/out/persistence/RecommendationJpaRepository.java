package com.ilililissue.opinion.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface RecommendationJpaRepository extends JpaRepository<RecommendationJpaEntity, Long> {

    boolean existsByOpinionIdAndMemberId(Long opinionId, Long memberId);

    @Modifying
    @Query("delete from RecommendationJpaEntity r where r.opinionId = :opinionId and r.memberId = :memberId")
    int deleteByOpinionIdAndMemberId(Long opinionId, Long memberId);

    @Query("select r.opinionId from RecommendationJpaEntity r "
            + "where r.memberId = :memberId and r.opinionId in :opinionIds")
    List<Long> findRecommendedOpinionIds(Long memberId, Collection<Long> opinionIds);
}
