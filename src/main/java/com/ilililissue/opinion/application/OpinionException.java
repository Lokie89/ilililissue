package com.ilililissue.opinion.application;

import com.ilililissue.common.DomainException;

public class OpinionException extends DomainException {

    private OpinionException(ErrorType type, String message) {
        super(type, message);
    }

    public static OpinionException notFound() {
        return new OpinionException(ErrorType.NOT_FOUND, "의견을 찾을 수 없습니다.");
    }

    public static OpinionException alreadyWritten() {
        return new OpinionException(ErrorType.CONFLICT, "이미 의견을 작성한 이슈입니다.");
    }

    public static OpinionException alreadyRecommended() {
        return new OpinionException(ErrorType.CONFLICT, "이미 추천한 의견입니다.");
    }
}
