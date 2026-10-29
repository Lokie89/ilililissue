package com.ilililissue.issue.adapter.in.web;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.issue.adapter.in.web.IssueResponses.CategoryResponse;
import com.ilililissue.issue.adapter.in.web.IssueResponses.IssueDetailResponse;
import com.ilililissue.issue.adapter.in.web.IssueResponses.IssueSummaryResponse;
import com.ilililissue.issue.application.port.in.IssueCommand;
import com.ilililissue.issue.application.port.in.IssueQuery;
import com.ilililissue.issue.application.port.in.ManageIssueUseCase;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.issue.domain.vo.IssueSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * 관리자 이슈 관리 API. {@code /api/admin/**}은 보안 설정에서 ADMIN만 허용한다.
 */
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@RestController
public class AdminIssueController {

    private final IssueQuery issueQuery;
    private final ManageIssueUseCase manageIssueUseCase;

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        CategoryResponse response = CategoryResponse.from(manageIssueUseCase.createCategory(request.name()));
        return ResponseEntity.created(URI.create("/api/categories/" + response.id())).body(response);
    }

    @GetMapping("/issues")
    public PageResult<IssueSummaryResponse> issues(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return issueQuery.getAllIssues(PageQuery.of(page, size)).map(IssueSummaryResponse::from);
    }

    @GetMapping("/issues/{issueId}")
    public IssueDetailResponse issue(@PathVariable long issueId) {
        return IssueDetailResponse.from(issueQuery.getIssue(new IssueId(issueId)));
    }

    @PostMapping("/issues")
    public ResponseEntity<IssueDetailResponse> create(@RequestBody IssueRequest request) {
        IssueId id = manageIssueUseCase.create(request.toCommand());
        return ResponseEntity.created(URI.create("/api/admin/issues/" + id.getValue()))
                .body(IssueDetailResponse.from(issueQuery.getIssue(id)));
    }

    @PutMapping("/issues/{issueId}")
    public IssueDetailResponse edit(@PathVariable long issueId, @RequestBody IssueRequest request) {
        IssueId id = new IssueId(issueId);
        manageIssueUseCase.edit(id, request.toCommand());
        return IssueDetailResponse.from(issueQuery.getIssue(id));
    }

    @PostMapping("/issues/{issueId}/review")
    public IssueDetailResponse review(@PathVariable long issueId) {
        IssueId id = new IssueId(issueId);
        manageIssueUseCase.review(id);
        return IssueDetailResponse.from(issueQuery.getIssue(id));
    }

    @PostMapping("/issues/{issueId}/publish")
    public IssueDetailResponse publish(@PathVariable long issueId) {
        IssueId id = new IssueId(issueId);
        manageIssueUseCase.publish(id);
        return IssueDetailResponse.from(issueQuery.getIssue(id));
    }

    @PostMapping("/issues/{issueId}/close")
    public IssueDetailResponse close(@PathVariable long issueId) {
        IssueId id = new IssueId(issueId);
        manageIssueUseCase.close(id);
        return IssueDetailResponse.from(issueQuery.getIssue(id));
    }

    @PostMapping("/issues/{issueId}/extend")
    public IssueDetailResponse extend(@PathVariable long issueId) {
        IssueId id = new IssueId(issueId);
        manageIssueUseCase.extend(id);
        return IssueDetailResponse.from(issueQuery.getIssue(id));
    }

    @PostMapping("/issues/{issueId}/correction")
    public IssueDetailResponse correct(@PathVariable long issueId, @RequestBody CorrectionRequest request) {
        IssueId id = new IssueId(issueId);
        manageIssueUseCase.correct(id, IssueSummary.from(request.summary()));
        return IssueDetailResponse.from(issueQuery.getIssue(id));
    }

    public record CategoryRequest(String name) {
    }

    public record IssueRequest(String title, String summary, Long categoryId, List<IssueCommand.Source> sources) {

        IssueCommand toCommand() {
            return IssueCommand.of(title, summary, categoryId, sources);
        }
    }

    public record CorrectionRequest(String summary) {
    }
}
