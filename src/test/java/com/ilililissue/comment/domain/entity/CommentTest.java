package com.ilililissue.comment.domain.entity;

import com.ilililissue.comment.domain.AlreadyEditedCommentException;
import com.ilililissue.comment.domain.vo.CommentContent;
import com.ilililissue.comment.domain.vo.Id;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CommentTest {

    @DisplayName("업데이트 된 댓글 업데이트 테스트")
    @Test
    void updateCommentAlreadyUpdated() {
        final CommentContent originComment = CommentContent.from("댓글");
        final CommentContent updateComment = CommentContent.from("(수정) 수정 댓글");

        Comment createdComment = Comment.of(originComment, null);
        Comment updatingComment = Comment.of(updateComment, null);

        createdComment.updateComment(updatingComment);

        Comment updatingTwiceComment = Comment.of(updateComment, null);

        Assertions.assertThatThrownBy(() -> createdComment.updateComment(updatingTwiceComment))
                .isInstanceOf(AlreadyEditedCommentException.class)
                .hasMessageContaining("이미 수정된 댓글");

    }

    @DisplayName("댓글 동일성 테스트")
    @Test
    void equals() {

        final Id originId = new Id(5);
        final CommentContent originContent = CommentContent.from("댓글11");


        Comment originComment = Comment.withId(originId, originContent, null, false, null, null);

        Comment sameIdAndContentComment = Comment.withId(originId, originContent, null, false, null, null);
        Assertions.assertThat(originComment).isEqualTo(sameIdAndContentComment);

        Id sameValueId = new Id(5);

        Comment sameValueIdAndContentComment = Comment.withId(sameValueId, originContent, null, false, null, null);
        Assertions.assertThat(originComment).isEqualTo(sameValueIdAndContentComment);


        final CommentContent sameContent = CommentContent.from("댓글11");

        Comment sameValueIdAndSameContentComment = Comment.withId(sameValueId, sameContent, null, false, null, null);
        Assertions.assertThat(originComment).isEqualTo(sameValueIdAndSameContentComment);

        final Id differentValueId = new Id(6);

        Comment differentValueIdAndSameContentComment = Comment.withId(differentValueId, sameContent, null, false, null, null);
        Assertions.assertThat(originComment).isNotEqualTo(differentValueIdAndSameContentComment);

    }
}
