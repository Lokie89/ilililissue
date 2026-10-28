package com.ilililissue.opinion.adapter.out.persistence;

import com.ilililissue.vote.domain.vo.Stance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OpinionJpaRepository extends JpaRepository<OpinionJpaEntity, Long> {

    Optional<OpinionJpaEntity> findByIssueIdAndAuthorId(Long issueId, Long authorId);

    Page<OpinionJpaEntity> findByIssueIdAndStanceAndHiddenFalseAndDeletedFalse(Long issueId, Stance stance, Pageable pageable);

    @Modifying
    @Query("update OpinionJpaEntity o set o.recommendationCount = o.recommendationCount + 1 where o.id = :id")
    int incrementRecommendationCount(Long id);

    @Modifying
    @Query("update OpinionJpaEntity o set o.recommendationCount = o.recommendationCount - 1 "
            + "where o.id = :id and o.recommendationCount > 0")
    int decrementRecommendationCount(Long id);
}
