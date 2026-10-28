package com.ilililissue.metrics.application.service;

import com.ilililissue.metrics.application.port.in.PurgeEventsUseCase;
import com.ilililissue.metrics.application.port.out.ParticipationEventPort;
import com.ilililissue.metrics.config.MetricsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class EventRetentionService implements PurgeEventsUseCase {

    private final ParticipationEventPort participationEventPort;
    private final MetricsProperties metricsProperties;
    private final Clock clock;

    @Transactional
    @Override
    public long purgeExpired() {
        LocalDateTime cutoff = LocalDateTime.now(clock).minus(metricsProperties.eventRetention());
        return participationEventPort.deleteOccurredBefore(cutoff);
    }
}
