package com.ilililissue.member.domain;

import com.ilililissue.common.DomainException;

public class AlreadyWithdrawnMemberException extends DomainException {

    private static final String message = "이미 탈퇴한 회원입니다.";

    public AlreadyWithdrawnMemberException() {
        super(ErrorType.CONFLICT, message);
    }
}
