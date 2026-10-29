package com.ilililissue.newsdraft.application;

import com.ilililissue.common.DomainException;

public class NewsDraftException extends DomainException {

    private NewsDraftException(ErrorType type, String message) {
        super(type, message);
    }

    public static NewsDraftException notConfigured(String message) {
        return new NewsDraftException(ErrorType.CONFLICT, message);
    }

    public static NewsDraftException alreadyRunning() {
        return new NewsDraftException(ErrorType.CONFLICT, "뉴스 초안 작성이 이미 진행 중입니다.");
    }

    public static NewsDraftException externalFailure(String message) {
        return new NewsDraftException(ErrorType.CONFLICT, message);
    }
}
