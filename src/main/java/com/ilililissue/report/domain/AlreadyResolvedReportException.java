package com.ilililissue.report.domain;

public class AlreadyResolvedReportException extends RuntimeException {

    private static final String message = "이미 처리된 신고입니다.";

    public AlreadyResolvedReportException() {
        super(message);
    }
}
