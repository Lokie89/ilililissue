package com.ilililissue.metrics.application.service;

import com.ilililissue.metrics.application.port.in.PurgeEventsUseCase;
import com.ilililissue.support.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class EventRetentionServiceTest extends ApiTestSupport {

    @Autowired
    PurgeEventsUseCase purgeEventsUseCase;

    private long insertEvent(long issueId, LocalDateTime occurredAt) {
        jdbcTemplate.update("INSERT INTO participation_event (type, issue_id, visitor_id, occurred_at) "
                + "VALUES ('ISSUE_VIEWED', ?, 'retention-test', ?)", issueId, occurredAt);
        return jdbcTemplate.queryForObject("SELECT max(id) FROM participation_event", Long.class);
    }

    private boolean exists(long id) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM participation_event WHERE id = ?", Long.class, id) > 0;
    }

    @DisplayName("보관 기간(기본 1년)이 지난 이용 기록만 지운다")
    @Test
    void purgeExpired() {
        LocalDateTime now = LocalDateTime.now();
        long expired = insertEvent(1, now.minusDays(400));
        long kept = insertEvent(1, now.minusDays(300));

        long deleted = purgeEventsUseCase.purgeExpired();

        assertThat(deleted).isGreaterThanOrEqualTo(1);
        assertThat(exists(expired)).isFalse();
        assertThat(exists(kept)).isTrue();
    }
}
