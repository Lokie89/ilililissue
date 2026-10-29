package com.ilililissue.newsdraft.adapter.out.openai;

import com.ilililissue.newsdraft.application.NewsDraftException;
import com.ilililissue.newsdraft.config.NewsDraftProperties;
import com.ilililissue.newsdraft.domain.IssueDraft;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

class OpenAiIssueDraftWriterTest {

    private final OpenAiIssueDraftWriter writer = new OpenAiIssueDraftWriter(
            new NewsDraftProperties(false, List.of(), 0, 0, 0, "key", null, null, null, null), JsonMapper.builder().build());

    @DisplayName("output_text의 JSON을 초안으로 바꾸고, 추론(reasoning) 항목은 건너뛴다")
    @Test
    void parsesOutputText() {
        String body = """
                {"status": "completed",
                 "usage": {"input_tokens": 1200, "output_tokens": 300},
                 "output": [
                   {"type": "reasoning", "summary": []},
                   {"type": "message", "role": "assistant", "content": [
                     {"type": "output_text", "text": "{\\"drafts\\":[{\\"title\\":\\"질문인가?\\",\\"summary\\":\\"배경\\",\\"keyPoint\\":\\"쟁점\\",\\"agreeReason\\":\\"찬성\\",\\"disagreeReason\\":\\"반대\\",\\"categoryId\\":3,\\"sourceUrls\\":[\\"https://a.example.com/1\\",\\"https://b.example.com/2\\"]}]}"}
                   ]}
                 ]}
                """;

        List<IssueDraft> drafts = writer.parseDrafts(body);

        Assertions.assertThat(drafts).containsExactly(new IssueDraft("질문인가?", "배경", 3,
                List.of("https://a.example.com/1", "https://b.example.com/2"), "쟁점", "찬성", "반대"));
    }

    @DisplayName("거절 응답이면 초안 0건으로 끝난다")
    @Test
    void refusal() {
        String body = """
                {"status": "completed", "output": [{"type": "message", "content": [{"type": "refusal", "refusal": "할 수 없습니다."}]}]}
                """;

        Assertions.assertThat(writer.parseDrafts(body)).isEmpty();
    }

    @DisplayName("응답이 끝까지 생성되지 않았거나 초안이 없으면 실패로 처리한다")
    @Test
    void incompleteOrMissing() {
        Assertions.assertThatThrownBy(() -> writer.parseDrafts("""
                        {"status": "incomplete", "incomplete_details": {"reason": "max_output_tokens"}, "output": []}
                        """))
                .isInstanceOf(NewsDraftException.class)
                .hasMessageContaining("max_output_tokens");
        Assertions.assertThatThrownBy(() -> writer.parseDrafts("""
                        {"status": "completed", "output": []}
                        """))
                .isInstanceOf(NewsDraftException.class);
    }
}
