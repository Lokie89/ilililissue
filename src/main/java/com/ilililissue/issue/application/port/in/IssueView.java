package com.ilililissue.issue.application.port.in;

import com.ilililissue.issue.domain.entity.Category;
import com.ilililissue.issue.domain.entity.Issue;

/**
 * 조회 응답용으로 이슈와 카테고리를 묶은 값.
 */
public record IssueView(Issue issue, Category category) {
}
