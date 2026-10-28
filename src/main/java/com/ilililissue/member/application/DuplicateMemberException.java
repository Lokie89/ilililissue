package com.ilililissue.member.application;

import com.ilililissue.common.DomainException;

public class DuplicateMemberException extends DomainException {

    private DuplicateMemberException(String message) {
        super(ErrorType.CONFLICT, message);
    }

    public static DuplicateMemberException email() {
        return new DuplicateMemberException("이미 가입된 이메일입니다.");
    }

    public static DuplicateMemberException nickname() {
        return new DuplicateMemberException("이미 사용 중인 닉네임입니다.");
    }
}
