package com.ilililissue.comment.domain.entity;

import com.ilililissue.comment.domain.AlreadyEditedCommentException;
import com.ilililissue.comment.domain.vo.CommentContent;
import com.ilililissue.comment.domain.vo.Id;
import com.ilililissue.comment.domain.vo.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * <h2>댓글 도메인</h2>
 * <li>
 * <ul>업데이트 된 댓글은 업데이트 할 수 없다.</ul>
 * </li>
 */
@EqualsAndHashCode(of = "id")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment {

    private Id id;
    private CommentContent content;
    private User user;
    private boolean updated;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public void updateComment(Comment updateComment) {
        if (updated) {
            throw new AlreadyEditedCommentException();
        }
        this.content = updateComment.content;
        this.updated = true;
    }

    public static Comment withId(Id id, CommentContent content, User user, boolean updated,
                                 LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return new Comment(id, content, user, updated, createdAt, modifiedAt);
    }

    public static Comment of(CommentContent content, User user) {
        return new Comment(null, content, user, false, null, null);
    }
}
