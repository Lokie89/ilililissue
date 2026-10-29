package com.ilililissue.newsdraft.application.port.in;

import com.ilililissue.issue.domain.vo.IssueId;

/**
 * 이슈 대표 이미지(AI 일러스트)를 만들어 붙인다. 한 장에 1~2분 걸리므로 호출하는 쪽에서 백그라운드로 돌린다.
 */
public interface GenerateIssueImageUseCase {

    /**
     * 현재 설정에서 이미지를 만들 수 있는지(Codex CLI 방식에서만 가능).
     */
    boolean available();

    /**
     * 이슈 내용으로 이미지를 만들어 붙인다. 이미 있으면 바꾼다.
     *
     * @return 만들었으면 true, 실패했으면 false(이슈는 그대로)
     */
    boolean generate(IssueId issueId);

    boolean isGenerating(IssueId issueId);
}
