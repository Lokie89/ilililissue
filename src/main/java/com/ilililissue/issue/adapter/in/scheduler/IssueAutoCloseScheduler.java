package com.ilililissue.issue.adapter.in.scheduler;

import com.ilililissue.issue.application.port.in.CloseExpiredIssuesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 06:59(Asia/Seoul)에 전날까지 게시된 이슈의 투표를 마감한다. 07:00 새 초안 작성 직전이다.
 * 서버가 한 대라는 전제다(여러 대여도 이미 마감된 이슈는 다시 고르지 않는다).
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class IssueAutoCloseScheduler {

    private final CloseExpiredIssuesUseCase closeExpiredIssuesUseCase;

    @Scheduled(cron = "0 59 6 * * *", zone = "Asia/Seoul")
    public void close() {
        int closed = closeExpiredIssuesUseCase.closePublishedBeforeToday();
        log.info("전날까지 게시된 이슈 {}건을 마감했습니다.", closed);
    }
}
