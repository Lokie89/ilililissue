package com.ilililissue.issue.application.port.in;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.issue.domain.entity.Category;
import com.ilililissue.issue.domain.vo.IssueId;

import java.util.List;

public interface IssueQuery {

    /**
     * 공개(게시, 마감) 이슈 목록.
     */
    PageResult<IssueView> getPublicIssues(PageQuery pageQuery);

    /**
     * 공개 이슈 상세. 게시 전 이슈는 찾을 수 없음으로 응답한다.
     */
    IssueView getPublicIssue(IssueId id);

    PageResult<IssueView> getAllIssues(PageQuery pageQuery);

    IssueView getIssue(IssueId id);

    List<Category> getCategories();
}
