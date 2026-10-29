package com.ilililissue.issue.application.port.out;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.issue.domain.entity.Issue;
import com.ilililissue.issue.domain.vo.CategoryId;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.issue.domain.vo.IssueStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IssuePort {

    Optional<Issue> findById(IssueId id);

    /**
     * 주어진 상태의 이슈를 게시일 최신순(게시 전 이슈는 id 역순)으로 조회한다.
     */
    PageResult<Issue> findByStatusIn(Collection<IssueStatus> statuses, PageQuery pageQuery);

    /**
     * 주어진 상태이면서 카테고리가 같은 이슈를 게시일 최신순으로 조회한다.
     */
    PageResult<Issue> findByStatusInAndCategory(Collection<IssueStatus> statuses, CategoryId categoryId, PageQuery pageQuery);

    /**
     * 게시 중(PUBLISHED)이고 마지막으로 열린 시각(연장했으면 연장 시각, 아니면 게시 시각)이 기준 시각보다 이른 이슈.
     */
    List<Issue> findPublishedBefore(LocalDateTime cutoff);

    Issue save(Issue issue);
}
