package com.ilililissue.opinion.domain;

public class NotOpinionAuthorException extends RuntimeException {

    private static final String message = "의견 작성자만 수정할 수 있습니다.";

    public NotOpinionAuthorException() {
        super(message);
    }
}
