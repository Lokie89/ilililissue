package com.ilililissue.issue.domain;

/**
 * @author SeongRok.Oh
 * @since 2023/08/21
 */
public class AccessDeniedExtensionException extends RuntimeException {

    private static final String errorMessage = "허가되지 않은 확장자 입니다.";

    public AccessDeniedExtensionException() {
        super(AccessDeniedExtensionException.errorMessage);
    }
}
