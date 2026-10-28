package com.ilililissue.metrics.adapter.in.scheduler;

import com.ilililissue.metrics.application.port.in.PurgeEventsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 04:00(Asia/Seoul)에 보관 기간이 지난 이용 기록을 지운다. 서버가 한 대라는 전제다(여러 대면 중복 실행된다).
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class EventRetentionScheduler {

    private final PurgeEventsUseCase purgeEventsUseCase;

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void purge() {
        long deleted = purgeEventsUseCase.purgeExpired();
        log.info("보관 기간이 지난 참여 이벤트 {}건을 지웠습니다.", deleted);
    }
}
