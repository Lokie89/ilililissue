package com.ilililissue.issue.adapter.in.web;

import com.ilililissue.issue.application.port.in.IssueView;
import com.ilililissue.issue.domain.entity.Category;
import com.ilililissue.issue.domain.entity.Issue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class IssueResponses {

    private IssueResponses() {
    }

    public record CategoryResponse(long id, String name) {

        public static CategoryResponse from(Category category) {
            return category == null ? null : new CategoryResponse(category.getId().getValue(), category.getName());
        }
    }

    public record IssueSummaryResponse(
            long id,
            String title,
            CategoryResponse category,
            String status,
            LocalDateTime publishedAt
    ) {

        public static IssueSummaryResponse from(IssueView view) {
            Issue issue = view.issue();
            return new IssueSummaryResponse(
                    issue.getId().getValue(),
                    issue.getTitle().getValue(),
                    CategoryResponse.from(view.category()),
                    issue.getStatus().name(),
                    issue.getPublishedAt()
            );
        }
    }

    public record IssueDetailResponse(
            long id,
            String title,
            String summary,
            CategoryResponse category,
            List<SourceResponse> sources,
            String status,
            LocalDateTime publishedAt,
            boolean corrected,
            LocalDateTime correctedAt
    ) {

        public static IssueDetailResponse from(IssueView view) {
            Issue issue = view.issue();
            return new IssueDetailResponse(
                    issue.getId().getValue(),
                    issue.getTitle().getValue(),
                    issue.getSummary().getValue(),
                    CategoryResponse.from(view.category()),
                    issue.getSources().getValues().stream()
                            .map(source -> new SourceResponse(source.getUrl(), source.getPublisher(),
                                    source.getPublishedOn()))
                            .toList(),
                    issue.getStatus().name(),
                    issue.getPublishedAt(),
                    issue.isCorrected(),
                    issue.getCorrectedAt()
            );
        }
    }

    public record SourceResponse(String url, String publisher, LocalDate publishedOn) {
    }
}
