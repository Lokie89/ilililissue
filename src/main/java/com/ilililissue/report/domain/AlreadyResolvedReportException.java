package com.ilililissue.report.domain;

import com.ilililissue.common.DomainException;

public class AlreadyResolvedReportException extends DomainException {

    private static final String message = "이미 처리된 신고입니다.";

    public AlreadyResolvedReportException() {
        super(ErrorType.CONFLICT, message);
    }
}
