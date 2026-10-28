package com.ilililissue.metrics.adapter.out.persistence;

import com.ilililissue.metrics.application.port.out.ParticipationEventPort;
import com.ilililissue.metrics.domain.ParticipationEvent;
import com.ilililissue.metrics.domain.ParticipationEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ParticipationEventPersistenceAdapter implements ParticipationEventPort {

    private final ParticipationEventJpaRepository repository;

    @Override
    public void save(ParticipationEvent event) {
        repository.save(new ParticipationEventJpaEntity(
                null,
                event.type(),
                event.issueId().getValue(),
                event.memberId() == null ? null : event.memberId().getValue(),
                event.stance(),
                event.targetStance(),
                event.occurredAt()
        ));
    }

    @Override
    public long count(ParticipationEventType type, LocalDateTime from, LocalDateTime to) {
        return repository.countByTypeAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(type, from, to);
    }

    @Override
    public long countDistinctMembers(ParticipationEventType type, LocalDateTime from, LocalDateTime to) {
        return repository.countDistinctMembers(type, from, to);
    }

    @Override
    public long countDistinctOpposingViewers(LocalDateTime from, LocalDateTime to) {
        return repository.countDistinctOpposingViewers(from, to);
    }

    @Override
    public List<MemberActivity> findMemberActivities(LocalDateTime until) {
        return repository.findMemberActivities(until).stream()
                .map(row -> new MemberActivity(row.getMemberId(), row.getFirstAt(), row.getLastAt()))
                .toList();
    }
}
