package com.ilililissue.member.application;

import com.ilililissue.common.DomainException;

public class MemberNotFoundException extends DomainException {

    private static final String message = "회원을 찾을 수 없습니다.";

    public MemberNotFoundException() {
        super(ErrorType.NOT_FOUND, message);
    }
}
