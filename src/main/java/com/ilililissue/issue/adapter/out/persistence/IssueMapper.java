package com.ilililissue.issue.adapter.out.persistence;

import com.ilililissue.issue.domain.entity.Issue;
import com.ilililissue.issue.domain.vo.Id;
import com.ilililissue.issue.domain.vo.IssueImage;
import com.ilililissue.issue.domain.vo.IssueImages;
import com.ilililissue.issue.domain.vo.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author SeongRok.Oh
 * @since 2023/08/21
 */
@Component
public class IssueMapper {

    public List<Issue> mapToDomain(List<IssueJpaEntity> issueEntities) {
        List<Issue> issues = issueEntities.stream().map(entity -> Issue.withId(
                new Id(entity.getId()),
                mapToIssueImageDomain(entity.getIssueImageJpaEntityList()),
                User.of(new Id(entity.getUserId()), null)
        )).collect(Collectors.toList());

        return issues;
    }

    private IssueImages mapToIssueImageDomain(List<IssueImageJpaEntity> issueImageEntities) {
        return IssueImages.from(
                issueImageEntities.stream()
                        .map(entity -> IssueImage.from(entity.getFileName()))
                        .collect(Collectors.toList())
        );
    }
}
