package com.ilililissue.issue.adapter.out.persistence;

import com.ilililissue.issue.domain.entity.Issue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author SeongRok.Oh
 * @since 2023/08/21
 */

@RequiredArgsConstructor
@Component
public class IssuePersistenceAdapter {

    private final IssueMapper issueMapper;
    private final IssueRepository issueRepository;

    public List<Issue> loadIssues() {
        List<IssueJpaEntity> issueJpaEntities = issueRepository.findAll();
        return issueMapper.mapToDomain(issueJpaEntities);
    }
}
