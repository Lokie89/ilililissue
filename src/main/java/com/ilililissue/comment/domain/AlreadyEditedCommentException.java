package com.ilililissue.comment.domain;

public class AlreadyEditedCommentException extends RuntimeException {

    private static final String message = "이미 수정된 댓글입니다.";

    public AlreadyEditedCommentException() {
        super(message);
    }
}
