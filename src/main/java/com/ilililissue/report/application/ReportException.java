package com.ilililissue.report.application;

import com.ilililissue.common.DomainException;

public class ReportException extends DomainException {

    private ReportException(ErrorType type, String message) {
        super(type, message);
    }

    public static ReportException notFound() {
        return new ReportException(ErrorType.NOT_FOUND, "신고를 찾을 수 없습니다.");
    }

    public static ReportException alreadyReported() {
        return new ReportException(ErrorType.CONFLICT, "이미 신고한 의견입니다.");
    }
}
