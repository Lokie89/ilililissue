package com.ilililissue.auth.application;

import com.ilililissue.common.DomainException;

public class AuthenticationFailedException extends DomainException {

    private AuthenticationFailedException(String message) {
        super(ErrorType.UNAUTHORIZED, message);
    }

    public static AuthenticationFailedException invalidCredentials() {
        return new AuthenticationFailedException("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    public static AuthenticationFailedException invalidRefreshToken() {
        return new AuthenticationFailedException("다시 로그인해 주세요.");
    }
}
