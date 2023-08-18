package com.ilililissue.comment.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public class CommentContent extends SelfValidating<CommentContent> {

    @NotBlank
    private final String content;

    private CommentContent(
            String content
    ) {
        this.content = content;
    }

    public static CommentContent from(String content) {
        return new CommentContent(content);
    }
}
