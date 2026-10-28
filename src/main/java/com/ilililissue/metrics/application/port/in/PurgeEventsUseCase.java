package com.ilililissue.metrics.application.port.in;

public interface PurgeEventsUseCase {

    /**
     * 보관 기간이 지난 참여 이벤트를 지운다.
     *
     * @return 지운 이벤트 수
     */
    long purgeExpired();
}
