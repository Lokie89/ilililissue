package com.ilililissue.issue.adapter.out.persistence;

import com.ilililissue.issue.domain.vo.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface IssueRepository extends JpaRepository<IssueJpaEntity, Long> {

    Page<IssueJpaEntity> findByStatusIn(Collection<IssueStatus> statuses, Pageable pageable);

    Page<IssueJpaEntity> findByStatusInAndCategoryId(Collection<IssueStatus> statuses, Long categoryId, Pageable pageable);

    /**
     * 연장했으면 연장 시각, 아니면 게시 시각이 기준보다 이른 이슈.
     */
    @Query("select i from IssueJpaEntity i where i.status = :status and coalesce(i.extendedAt, i.publishedAt) < :cutoff")
    List<IssueJpaEntity> findOpenedBefore(@Param("status") IssueStatus status, @Param("cutoff") LocalDateTime cutoff);
}
