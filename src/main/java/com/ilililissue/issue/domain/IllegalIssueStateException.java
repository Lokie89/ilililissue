package com.ilililissue.issue.domain;

import com.ilililissue.common.DomainException;
import com.ilililissue.issue.domain.vo.IssueStatus;

public class IllegalIssueStateException extends DomainException {

    public IllegalIssueStateException(IssueStatus current, String action) {
        super(ErrorType.CONFLICT, "%s 상태의 이슈는 %s 할 수 없습니다.".formatted(current, action));
    }

    public IllegalIssueStateException(String message) {
        super(ErrorType.CONFLICT, message);
    }
}
