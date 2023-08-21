package com.ilililissue.issue.adapter;

import com.ilililissue.issue.adapter.out.persistence.IssueImageJpaEntity;
import com.ilililissue.issue.adapter.out.persistence.IssueJpaEntity;
import com.ilililissue.issue.adapter.out.persistence.IssueMapper;
import com.ilililissue.issue.adapter.out.persistence.IssuePersistenceAdapter;
import com.ilililissue.issue.adapter.out.persistence.IssueRepository;
import com.ilililissue.issue.domain.entity.Issue;
import com.ilililissue.issue.domain.vo.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.when;

/**
 * @author SeongRok.Oh
 * @since 2023/08/21
 */

@ExtendWith(MockitoExtension.class)
public class IssuePersistenceAdapterTest {

    @Mock
    IssueRepository issueRepository;

    @Mock
    IssueMapper issueMapper;

    @InjectMocks
    IssuePersistenceAdapter issuePersistenceAdapter;

    private final IssueJpaEntity issueJpaEntity = new IssueJpaEntity(
            5L,
            List.of(new IssueImageJpaEntity(3L, "53df314-53sddf-134t35", 5L)),
            6L
    );

    private final Issue mockIssue
            = Issue.withId(
            new Id(5),
            IssueImages.from(IssueImage.of("53df314-53sddf-134t35", ImageExtension.from("jpg"))),
            User.of(new Id(6), null)
    );

    @DisplayName("이슈 저장소 조회 테스트")
    @Test
    void loadIssues() {

        when(issueRepository.findAll()).thenReturn(List.of(issueJpaEntity));
        when(issueMapper.mapToDomain(List.of(issueJpaEntity))).thenReturn(List.of(mockIssue));

        List<Issue> issues = issuePersistenceAdapter.loadIssues();

        Assertions.assertThat(issues.contains(mockIssue)).isTrue();
    }
}
