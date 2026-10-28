package com.ilililissue.issue.application.port.in;

import com.ilililissue.issue.domain.entity.Category;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.issue.domain.vo.IssueSummary;

/**
 * 관리자 이슈 관리. 호출 권한(ADMIN)은 웹 계층의 보안 설정에서 검증한다.
 */
public interface ManageIssueUseCase {

    Category createCategory(String name);

    IssueId create(IssueCommand command);

    void edit(IssueId id, IssueCommand command);

    void review(IssueId id);

    void publish(IssueId id);

    void close(IssueId id);

    void correct(IssueId id, IssueSummary correctedSummary);
}
