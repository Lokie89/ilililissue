package com.ilililissue.comment.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CommentTest {

    @DisplayName("업데이트 된 댓글 업데이트 테스트")
    @Test
    void updateCommentAlreadyUpdated() {
        final Long issueId = 53L;
        final String comment = "(수정) 수정 댓글";

        Comment updateComment = Comment.builder()
                .issueId(issueId)
                .content(comment)
                .build();

        Comment updatedComment = Comment.builder()
                .updated(true)
                .build();

        Assertions.assertThatThrownBy(() -> updatedComment.updateComment(updateComment))
                .isInstanceOf(AlreadyEditedCommentException.class)
                .hasMessageContaining("이미 수정된 댓글");
    }
}
