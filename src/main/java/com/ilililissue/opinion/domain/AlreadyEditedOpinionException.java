package com.ilililissue.opinion.domain;

import com.ilililissue.common.DomainException;

public class AlreadyEditedOpinionException extends DomainException {

    private static final String message = "이미 수정된 의견입니다.";

    public AlreadyEditedOpinionException() {
        super(ErrorType.CONFLICT, message);
    }
}
