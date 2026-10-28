package com.ilililissue.opinion.domain;

import com.ilililissue.common.DomainException;

public class NotOpinionAuthorException extends DomainException {

    private static final String message = "의견 작성자만 수정할 수 있습니다.";

    public NotOpinionAuthorException() {
        super(ErrorType.FORBIDDEN, message);
    }
}
