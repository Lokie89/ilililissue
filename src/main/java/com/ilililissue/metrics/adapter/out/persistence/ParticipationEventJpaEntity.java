package com.ilililissue.metrics.adapter.out.persistence;

import com.ilililissue.metrics.domain.ParticipationEventType;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "participation_event", indexes = {
        @Index(name = "idx_event_type_time", columnList = "type, occurredAt"),
        @Index(name = "idx_event_member_time", columnList = "memberId, occurredAt"),
        @Index(name = "idx_event_visitor", columnList = "issueId, visitorId")})
@Entity
public class ParticipationEventJpaEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipationEventType type;

    @Column(nullable = false)
    private Long issueId;

    private Long memberId;

    @Column(length = 36)
    private String visitorId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Stance stance;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Stance targetStance;

    @Column(nullable = false)
    private LocalDateTime occurredAt;
}
