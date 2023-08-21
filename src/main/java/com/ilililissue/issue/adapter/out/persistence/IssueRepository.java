package com.ilililissue.issue.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author SeongRok.Oh
 * @since 2023/08/21
 */
public interface IssueRepository extends JpaRepository<IssueJpaEntity, Long> {
}
