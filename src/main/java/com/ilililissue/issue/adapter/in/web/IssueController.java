package com.ilililissue.issue.adapter.in.web;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.issue.adapter.in.web.IssueResponses.CategoryResponse;
import com.ilililissue.issue.adapter.in.web.IssueResponses.IssueDetailResponse;
import com.ilililissue.issue.adapter.in.web.IssueResponses.IssueSummaryResponse;
import com.ilililissue.issue.application.port.in.IssueQuery;
import com.ilililissue.issue.domain.vo.IssueId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 비회원도 접근할 수 있는 이슈 조회 API.
 */
@RequiredArgsConstructor
@RestController
public class IssueController {

    private final IssueQuery issueQuery;

    @GetMapping("/api/categories")
    public List<CategoryResponse> categories() {
        return issueQuery.getCategories().stream().map(CategoryResponse::from).toList();
    }

    @GetMapping("/api/issues")
    public PageResult<IssueSummaryResponse> issues(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return issueQuery.getPublicIssues(PageQuery.of(page, size)).map(IssueSummaryResponse::from);
    }

    @GetMapping("/api/issues/{issueId}")
    public IssueDetailResponse issue(@PathVariable long issueId) {
        return IssueDetailResponse.from(issueQuery.getPublicIssue(new IssueId(issueId)));
    }
}
