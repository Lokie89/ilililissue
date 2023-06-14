package com.ilililissue.comment.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentContent {
    private final String content;

    public static CommentContent from(String content) {
        return new CommentContent(content);
    }
}
