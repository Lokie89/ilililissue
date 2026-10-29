package com.ilililissue.issue.adapter.in.web;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.issue.adapter.in.web.IssueResponses.CategoryResponse;
import com.ilililissue.issue.adapter.in.web.IssueResponses.IssueDetailResponse;
import com.ilililissue.issue.adapter.in.web.IssueResponses.IssueSummaryResponse;
import com.ilililissue.issue.application.port.in.IssueQuery;
import com.ilililissue.issue.application.port.in.IssueView;
import com.ilililissue.issue.domain.vo.CategoryId;
import com.ilililissue.issue.domain.vo.IssueId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
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
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) Long categoryId) {
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<IssueView> issues = categoryId == null
                ? issueQuery.getPublicIssues(pageQuery)
                : issueQuery.getPublicIssues(new CategoryId(categoryId), pageQuery);
        return issues.map(IssueSummaryResponse::from);
    }

    /**
     * 대표 이미지. 키가 바뀌면 URL(?v=키)도 바뀌므로 오래 캐시한다.
     */
    @GetMapping(value = "/api/issues/{issueId}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> image(@PathVariable long issueId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                .body(issueQuery.getPublicImage(new IssueId(issueId)));
    }

    @GetMapping("/api/issues/{issueId}")
    public IssueDetailResponse issue(@PathVariable long issueId) {
        return IssueDetailResponse.from(issueQuery.getPublicIssue(new IssueId(issueId)));
    }
}
