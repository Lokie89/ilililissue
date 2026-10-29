package com.ilililissue.newsdraft.application.port.out;

import java.util.Optional;

/**
 * 이슈 대표 이미지(일러스트)를 만든다.
 */
public interface IssueImageGeneratorPort {

    /**
     * 이미지를 만들 수 있는 설정인지(예: Codex CLI 방식).
     */
    boolean available();

    /**
     * @return JPEG. 만들지 못했으면 빈 값
     */
    Optional<byte[]> generate(String title, String summary, String categoryName);
}
