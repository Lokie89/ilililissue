package com.ilililissue.comment.adapter.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Table(name = "comment", uniqueConstraints = {
        @UniqueConstraint(name = "UniqueCommentByIssueAndUser", columnNames = {"issueId", "userId"})})
@Entity
public class CommentJpaEntity extends AuditedJpaEntity {

    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private Long issueId;
    private Long userId;
    private String content;
    private boolean updated;

    public static CommentJpaEntity withoutId(String comment,
                                             Long issueId,
                                             Long userId,
                                             boolean updated) {
        CommentJpaEntity entity = new CommentJpaEntity();
        entity.content = comment;
        entity.issueId = issueId;
        entity.userId = userId;
        entity.updated = updated;
        return entity;
    }
}
