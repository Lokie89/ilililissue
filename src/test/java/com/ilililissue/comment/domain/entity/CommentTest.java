package com.ilililissue.comment.domain.entity;

import com.ilililissue.comment.domain.AlreadyEditedCommentException;
import com.ilililissue.comment.domain.vo.Id;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CommentTest {

    @DisplayName("업데이트 된 댓글 업데이트 테스트")
    @Test
    void updateCommentAlreadyUpdated() {
        final long issueId = 53L;
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

    @DisplayName("댓글 동일성 테스트")
    @Test
    void equals() {

        final Id originId = new Id(5);
        final String originContent = "댓글11";


        Comment originComment = Comment.builder()
                .id(originId)
                .content(originContent)
                .build();

        Comment sameIdAndContentComment = Comment.builder()
                .id(originId)
                .content(originContent)
                .build();
        Assertions.assertThat(originComment).isEqualTo(sameIdAndContentComment);

        Id sameValueId = new Id(5);

        Comment sameValueIdAndContentComment = Comment.builder()
                .id(sameValueId)
                .content(originContent)
                .build();
        Assertions.assertThat(originComment).isEqualTo(sameValueIdAndContentComment);


        final String sameContent = "댓글11";

        Comment sameValueIdAndSameContentComment = Comment.builder()
                .id(sameValueId)
                .content(sameContent)
                .build();
        Assertions.assertThat(originComment).isEqualTo(sameValueIdAndSameContentComment);

        Id differentValueId = new Id(6);

        Comment differentValueIdAndSameContentComment = Comment.builder()
                .id(differentValueId)
                .content(sameContent)
                .build();
        Assertions.assertThat(originComment).isNotEqualTo(differentValueIdAndSameContentComment);

    }
}
