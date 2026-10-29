package com.ilililissue.newsdraft.adapter.out.openai;

import com.ilililissue.newsdraft.application.port.out.IssueImageGeneratorPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * OpenAI API 방식(기본)에서는 아직 이미지를 만들지 않는다. 이미지는 Codex CLI 방식에서만 만든다.
 */
@ConditionalOnProperty(name = "news-draft.writer", havingValue = "openai-api", matchIfMissing = true)
@Component
public class NoIssueImageGenerator implements IssueImageGeneratorPort {

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public Optional<byte[]> generate(String title, String summary, String categoryName) {
        return Optional.empty();
    }
}
