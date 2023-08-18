package com.ilililissue.comment.domain.entity;

import com.ilililissue.comment.domain.AlreadyEditedCommentException;
import com.ilililissue.comment.domain.vo.CommentContent;
import com.ilililissue.comment.domain.vo.Id;
import com.ilililissue.comment.domain.vo.User;
import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * <h2>댓글 도메인</h2>
 * <li>
 * <ul>업데이트 된 댓글은 업데이트 할 수 없다.</ul>
 * </li>
 */
@EqualsAndHashCode(of = "id", callSuper = false)
public class Comment extends SelfValidating<Comment> {

    private Id id;

    @NotNull
    private CommentContent content;

    @NotNull
    private User register;
    private boolean updated;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private Comment(
            Id id,
            CommentContent content,
            User register,
            boolean updated,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt
    ) {
        this.id = id;
        this.content = content;
        this.register = register;
        this.updated = updated;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.validate();
    }

    public void updateComment(Comment updateComment) {
        if (updated) {
            throw new AlreadyEditedCommentException();
        }
        this.content = updateComment.content;
        this.updated = true;
    }

    public static Comment withId(Id id, CommentContent content, User register, boolean updated,
                                 LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return new Comment(id, content, register, updated, createdAt, modifiedAt);
    }

    public static Comment of(CommentContent content, User register) {
        return new Comment(null, content, register, false, null, null);
    }
}
