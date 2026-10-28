package com.ilililissue.issue.application.port.out;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.issue.domain.entity.Issue;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.issue.domain.vo.IssueStatus;

import java.util.Collection;
import java.util.Optional;

public interface IssuePort {

    Optional<Issue> findById(IssueId id);

    /**
     * 주어진 상태의 이슈를 게시일 최신순(게시 전 이슈는 id 역순)으로 조회한다.
     */
    PageResult<Issue> findByStatusIn(Collection<IssueStatus> statuses, PageQuery pageQuery);

    Issue save(Issue issue);
}
