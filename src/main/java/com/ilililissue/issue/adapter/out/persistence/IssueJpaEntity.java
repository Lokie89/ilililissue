package com.ilililissue.issue.adapter.out.persistence;

import com.ilililissue.issue.domain.vo.IssueStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "issue", indexes = @Index(name = "idx_issue_status_published_at", columnList = "status, publishedAt"))
@Entity
public class IssueJpaEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 1000)
    private String summary;

    @Column(nullable = false)
    private Long categoryId;

    @BatchSize(size = 50)
    @ElementCollection
    @CollectionTable(name = "issue_source", joinColumns = @JoinColumn(name = "issueId"))
    @OrderColumn(name = "position")
    private List<IssueSourceEmbeddable> sources = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueStatus status;

    private LocalDateTime publishedAt;

    private LocalDateTime correctedAt;
}
