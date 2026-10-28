package com.ilililissue.member.application;

import com.ilililissue.common.DomainException;

public class PasswordMismatchException extends DomainException {

    private static final String message = "비밀번호가 일치하지 않습니다.";

    public PasswordMismatchException() {
        super(ErrorType.BAD_REQUEST, message);
    }
}
