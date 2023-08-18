package com.ilililissue.issue.domain;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
public class UnauthorizedException extends RuntimeException {
    private static final String errorMessage = "권한이 없습니다.";

    public UnauthorizedException() {
        super(UnauthorizedException.errorMessage);
    }
}
