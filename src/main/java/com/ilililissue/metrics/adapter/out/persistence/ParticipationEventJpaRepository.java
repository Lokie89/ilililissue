package com.ilililissue.metrics.adapter.out.persistence;

import com.ilililissue.metrics.domain.ParticipationEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ParticipationEventJpaRepository extends JpaRepository<ParticipationEventJpaEntity, Long> {

    long countByTypeAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(ParticipationEventType type,
                                                                       LocalDateTime from, LocalDateTime to);

    @Query("select count(distinct e.memberId) from ParticipationEventJpaEntity e "
            + "where e.type = :type and e.occurredAt >= :from and e.occurredAt < :to and e.memberId is not null")
    long countDistinctMembers(ParticipationEventType type, LocalDateTime from, LocalDateTime to);

    @Query("select count(distinct e.memberId) from ParticipationEventJpaEntity e "
            + "where e.type = com.ilililissue.metrics.domain.ParticipationEventType.OPINIONS_VIEWED "
            + "and e.occurredAt >= :from and e.occurredAt < :to "
            + "and e.stance is not null and e.targetStance <> e.stance")
    long countDistinctOpposingViewers(LocalDateTime from, LocalDateTime to);

    @Query("select e.memberId as memberId, min(e.occurredAt) as firstAt, max(e.occurredAt) as lastAt "
            + "from ParticipationEventJpaEntity e "
            + "where e.memberId is not null and e.occurredAt < :until group by e.memberId")
    List<ActivityRow> findMemberActivities(LocalDateTime until);

    interface ActivityRow {

        Long getMemberId();

        LocalDateTime getFirstAt();

        LocalDateTime getLastAt();
    }
}
