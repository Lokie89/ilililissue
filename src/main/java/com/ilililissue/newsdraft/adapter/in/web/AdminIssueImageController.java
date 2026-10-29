package com.ilililissue.newsdraft.adapter.in.web;

import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.newsdraft.application.NewsDraftException;
import com.ilililissue.newsdraft.application.port.in.GenerateIssueImageUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이슈 대표 이미지 다시 만들기. 한 장에 1~2분 걸려 백그라운드로 돌리고 202로 응답한다.
 * 끝나면 이슈의 {@code imageKey}가 바뀐다.
 */
@RequestMapping("/api/admin/issues/{issueId}/image")
@RestController
public class AdminIssueImageController {

    private final GenerateIssueImageUseCase generateIssueImageUseCase;
    private final TaskExecutor taskExecutor;

    public AdminIssueImageController(GenerateIssueImageUseCase generateIssueImageUseCase,
                                     @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor) {
        this.generateIssueImageUseCase = generateIssueImageUseCase;
        this.taskExecutor = taskExecutor;
    }

    @PostMapping
    public ResponseEntity<ImageStatusResponse> generate(@PathVariable long issueId) {
        if (!generateIssueImageUseCase.available()) {
            throw NewsDraftException.notConfigured("이미지 생성은 Codex CLI 방식(NEWS_DRAFT_WRITER=codex-cli)에서만 할 수 있습니다.");
        }
        IssueId id = new IssueId(issueId);
        if (generateIssueImageUseCase.isGenerating(id)) {
            throw NewsDraftException.alreadyRunning();
        }
        taskExecutor.execute(() -> generateIssueImageUseCase.generate(id));
        return ResponseEntity.accepted().body(new ImageStatusResponse(true));
    }

    @GetMapping("/status")
    public ImageStatusResponse status(@PathVariable long issueId) {
        return new ImageStatusResponse(generateIssueImageUseCase.isGenerating(new IssueId(issueId)));
    }

    public record ImageStatusResponse(boolean generating) {
    }
}
