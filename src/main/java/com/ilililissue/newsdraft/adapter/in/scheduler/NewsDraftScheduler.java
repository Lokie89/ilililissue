package com.ilililissue.newsdraft.adapter.in.scheduler;

import com.ilililissue.newsdraft.application.port.in.GenerateNewsDraftsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * {@code news-draft.enabled=true}이면 매일 07:00(Asia/Seoul)에 지난 24시간 기사로 이슈 초안을 만든다.
 * 서버가 한 대라는 전제다(여러 대면 중복 실행된다).
 */
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "news-draft.enabled", havingValue = "true")
@Component
public class NewsDraftScheduler {

    private final GenerateNewsDraftsUseCase generateNewsDraftsUseCase;

    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Seoul")
    public void generate() {
        try {
            generateNewsDraftsUseCase.generate();
        } catch (RuntimeException e) {
            log.error("뉴스 초안 자동 실행 실패: {}", e.getMessage(), e);
        }
    }
}
