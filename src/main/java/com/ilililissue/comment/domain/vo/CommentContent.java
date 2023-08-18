package com.ilililissue.comment.domain.vo;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentContent {
    private final String content;

    public static CommentContent from(String content) {
        return new CommentContent(content);
    }
}
