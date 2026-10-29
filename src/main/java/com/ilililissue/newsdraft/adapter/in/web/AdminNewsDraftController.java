package com.ilililissue.newsdraft.adapter.in.web;

import com.ilililissue.newsdraft.application.NewsDraftException;
import com.ilililissue.newsdraft.application.port.in.GenerateNewsDraftsUseCase;
import com.ilililissue.newsdraft.application.port.in.GenerateNewsDraftsUseCase.NewsDraftRun;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 뉴스 자동 초안 수동 실행. AI 호출이 수십 초~수 분 걸려 백그라운드로 돌리고 202로 응답한다.
 * 결과는 {@code GET /api/admin/news-drafts/status}와 이슈 목록(DRAFT)에서 확인한다.
 */
@Slf4j
@RequestMapping("/api/admin/news-drafts")
@RestController
public class AdminNewsDraftController {

    private final GenerateNewsDraftsUseCase generateNewsDraftsUseCase;
    private final TaskExecutor taskExecutor;

    public AdminNewsDraftController(GenerateNewsDraftsUseCase generateNewsDraftsUseCase,
                                    @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor) {
        this.generateNewsDraftsUseCase = generateNewsDraftsUseCase;
        this.taskExecutor = taskExecutor;
    }

    @PostMapping
    public ResponseEntity<StatusResponse> start() {
        if (generateNewsDraftsUseCase.isRunning()) {
            throw NewsDraftException.alreadyRunning();
        }
        taskExecutor.execute(() -> {
            try {
                generateNewsDraftsUseCase.generate();
            } catch (RuntimeException e) {
                log.warn("뉴스 초안 수동 실행 실패: {}", e.getMessage());
            }
        });
        return ResponseEntity.accepted().body(new StatusResponse(true, generateNewsDraftsUseCase.lastRun().orElse(null)));
    }

    @GetMapping("/status")
    public StatusResponse status() {
        return new StatusResponse(generateNewsDraftsUseCase.isRunning(), generateNewsDraftsUseCase.lastRun().orElse(null));
    }

    /**
     * @param lastRun 서버가 켜진 뒤 마지막 실행 결과. 없으면 null
     */
    public record StatusResponse(boolean running, NewsDraftRun lastRun) {
    }
}
