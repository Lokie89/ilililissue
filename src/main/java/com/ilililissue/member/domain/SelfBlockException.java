package com.ilililissue.member.domain;

import com.ilililissue.common.DomainException;

public class SelfBlockException extends DomainException {

    private static final String message = "자기 자신은 차단할 수 없습니다.";

    public SelfBlockException() {
        super(ErrorType.BAD_REQUEST, message);
    }
}
