package com.ilililissue.comment.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * <h2>댓글 도메인</h2>
 * <li>
 * <ul>업데이트 된 댓글은 업데이트 할 수 없다.</ul>
 * </li>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment {

    private Long id;
    private CommentContent content;
    private boolean updated;
    private long userId;
    private long issueId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;


    public static Builder builder() {
        return new Builder();
    }

    public void updateComment(Comment updateComment) {
        if (updated) {
            throw new AlreadyEditedCommentException();
        }
        this.content = updateComment.content;
    }

    public static class Builder {
        private Long id;
        private CommentContent content;
        private boolean updated;
        private long userId;
        private long issueId;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder content(String content) {
            this.content = CommentContent.from(content);
            return this;
        }

        public Builder updated(boolean updated) {
            this.updated = updated;
            return this;
        }

        public Builder userId(long userId) {
            this.userId = userId;
            return this;
        }

        public Builder issueId(long issueId) {
            this.issueId = issueId;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder modifiedAt(LocalDateTime modifiedAt) {
            this.modifiedAt = modifiedAt;
            return this;
        }

        public Comment build() {
            return new Comment(this.id, this.content, this.updated, this.userId, this.issueId,
                    this.createdAt, this.modifiedAt);
        }

    }
}
