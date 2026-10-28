package com.ilililissue.opinion.adapter.out.persistence;

import com.ilililissue.vote.domain.vo.Stance;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 추천순 정렬을 위해 추천 수(recommendationCount)를 비정규화해 보관한다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "opinion",
        uniqueConstraints = @UniqueConstraint(name = "uk_opinion_issue_author", columnNames = {"issueId", "authorId"}),
        indexes = {
                @Index(name = "idx_opinion_recommended", columnList = "issueId, stance, hidden, deleted, recommendationCount"),
                @Index(name = "idx_opinion_latest", columnList = "issueId, stance, hidden, deleted, writtenAt")})
@Entity
public class OpinionJpaEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false)
    private Long issueId;

    @Column(nullable = false)
    private Long authorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Stance stance;

    @Column(nullable = false, length = 500)
    private String content;

    private boolean edited;

    private boolean hidden;

    private boolean deleted;

    private long recommendationCount;

    @Column(nullable = false)
    private LocalDateTime writtenAt;

    public void update(String content, boolean edited, boolean hidden, boolean deleted) {
        this.content = content;
        this.edited = edited;
        this.hidden = hidden;
        this.deleted = deleted;
    }
}
