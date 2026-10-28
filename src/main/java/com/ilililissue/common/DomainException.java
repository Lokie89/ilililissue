package com.ilililissue.common;

/**
 * 웹 응답 상태로 변환되는 애플리케이션 예외의 공통 부모.
 */
public abstract class DomainException extends RuntimeException {

    private final ErrorType type;

    protected DomainException(ErrorType type, String message) {
        super(message);
        this.type = type;
    }

    public ErrorType getType() {
        return this.type;
    }

    public enum ErrorType {
        BAD_REQUEST,
        UNAUTHORIZED,
        FORBIDDEN,
        NOT_FOUND,
        CONFLICT,
    }
}
