package com.ilililissue.opinion.domain;

public class AlreadyEditedOpinionException extends RuntimeException {

    private static final String message = "이미 수정된 의견입니다.";

    public AlreadyEditedOpinionException() {
        super(message);
    }
}
