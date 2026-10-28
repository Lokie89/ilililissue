package com.ilililissue.metrics.application.port.in;

import java.time.LocalDate;

public interface MetricsQuery {

    /**
     * from ~ to (양 끝 포함, Asia/Seoul 기준 날짜) 기간의 참여 지표.
     */
    ParticipationMetrics getMetrics(LocalDate from, LocalDate to);
}
