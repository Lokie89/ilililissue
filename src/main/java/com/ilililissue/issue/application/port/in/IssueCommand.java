package com.ilililissue.issue.application.port.in;

import com.ilililissue.issue.domain.vo.CategoryId;
import com.ilililissue.issue.domain.vo.IssueSource;
import com.ilililissue.issue.domain.vo.IssueSources;
import com.ilililissue.issue.domain.vo.IssueSummary;
import com.ilililissue.issue.domain.vo.IssueTitle;

import java.time.LocalDate;
import java.util.List;

/**
 * 이슈 작성·수정 입력. 생성 시 각 값 객체가 스스로 검증한다.
 */
public record IssueCommand(
        IssueTitle title,
        IssueSummary summary,
        CategoryId categoryId,
        IssueSources sources
) {

    public static IssueCommand of(String title, String summary, Long categoryId, List<Source> sources) {
        return new IssueCommand(
                IssueTitle.from(title),
                IssueSummary.from(summary),
                new CategoryId(categoryId == null ? 0 : categoryId),
                IssueSources.from(sources == null ? List.of() : sources.stream()
                        .map(source -> IssueSource.of(source.url(), source.publisher(), source.publishedOn()))
                        .toList())
        );
    }

    public record Source(String url, String publisher, LocalDate publishedOn) {
    }
}
