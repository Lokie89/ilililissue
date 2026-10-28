package com.ilililissue.issue.adapter.out.persistence;

import com.ilililissue.issue.domain.vo.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface IssueRepository extends JpaRepository<IssueJpaEntity, Long> {

    Page<IssueJpaEntity> findByStatusIn(Collection<IssueStatus> statuses, Pageable pageable);
}
