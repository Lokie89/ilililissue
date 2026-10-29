package com.ilililissue.issue.domain;

import com.ilililissue.issue.domain.entity.Issue;
import com.ilililissue.issue.domain.vo.CategoryId;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.issue.domain.vo.IssueSource;
import com.ilililissue.issue.domain.vo.IssueSources;
import com.ilililissue.issue.domain.vo.IssueStatus;
import com.ilililissue.issue.domain.vo.IssueSummary;
import com.ilililissue.issue.domain.vo.IssueTitle;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class IssueFixture {

    public static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 28, 9, 0);

    public static IssueSources sources() {
        return IssueSources.from(IssueSource.of("https://news.example.com/a/1", "예시일보", LocalDate.of(2026, 10, 27)));
    }

    public static Issue draft(IssueSources sources) {
        return Issue.draft(IssueTitle.from("주 4일제를 도입해야 할까?"), IssueSummary.from("배경 설명"),
                new CategoryId(1), sources);
    }

    public static Issue withStatus(long id, IssueStatus status) {
        return Issue.withId(new IssueId(id), IssueTitle.from("주 4일제를 도입해야 할까?"), IssueSummary.from("배경 설명"),
                new CategoryId(1), sources(), status, status == IssueStatus.DRAFT ? null : NOW, null, null);
    }
}
