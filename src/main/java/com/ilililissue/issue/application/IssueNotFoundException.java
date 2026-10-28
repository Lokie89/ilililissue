package com.ilililissue.issue.application;

import com.ilililissue.common.DomainException;

public class IssueNotFoundException extends DomainException {

    private static final String message = "이슈를 찾을 수 없습니다.";

    public IssueNotFoundException() {
        super(ErrorType.NOT_FOUND, message);
    }
}
