package com.ilililissue.newsdraft.application.port.in;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 뉴스를 모아 이슈 초안(DRAFT)을 등록한다. 게시는 관리자가 검수한 뒤 한다.
 */
public interface GenerateNewsDraftsUseCase {

    /**
     * 지난 24시간 기사로 초안을 만든다. 이미 실행 중이면 {@code NewsDraftException}(409).
     */
    NewsDraftRun generate();

    boolean isRunning();

    /**
     * 서버가 켜진 뒤 마지막 실행 결과(메모리에만 보관).
     */
    Optional<NewsDraftRun> lastRun();

    /**
     * @param error 실패했으면 사유, 성공이면 null
     */
    record NewsDraftRun(
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            int articleCount,
            List<Long> createdIssueIds,
            int skippedDrafts,
            String error
    ) {
    }
}
