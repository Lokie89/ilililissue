package com.ilililissue.comment.domain.vo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
public class CommentContentTest {

    @Test
    void equals() {
        final String originContent = "테스트 내용";
        CommentContent originCommentContent = CommentContent.from(originContent);

        CommentContent sameContentCommentContent = CommentContent.from(originContent);
        Assertions.assertThat(originCommentContent).isEqualTo(sameContentCommentContent);

        final String sameValueContent = "테스트 내용";
        CommentContent sameValueContentCommentContent = CommentContent.from(sameValueContent);
        Assertions.assertThat(originCommentContent).isEqualTo(sameValueContentCommentContent);
    }
}
