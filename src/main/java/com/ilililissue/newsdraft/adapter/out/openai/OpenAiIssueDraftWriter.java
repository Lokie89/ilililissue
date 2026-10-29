package com.ilililissue.newsdraft.adapter.out.openai;

import com.ilililissue.issue.domain.entity.Category;
import com.ilililissue.newsdraft.application.NewsDraftException;
import com.ilililissue.newsdraft.application.port.out.IssueDraftWriterPort;
import com.ilililissue.newsdraft.config.NewsDraftProperties;
import com.ilililissue.newsdraft.domain.IssueDraft;
import com.ilililissue.newsdraft.domain.NewsArticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Responses API(구조화 출력)로 이슈 초안을 쓴다.
 * 인터페이스: {@code POST https://api.openai.com/v1/responses}, 헤더 {@code Authorization: Bearer <OPENAI_API_KEY>},
 * 본문 {@code {model, instructions, input, text.format{type: json_schema, name, schema, strict: true}}}.
 * 응답은 {@code output[type=message].content[type=output_text].text}의 JSON, 거절이면 {@code content[type=refusal]}.
 * 기사 제목·요약문은 프롬프트 입력으로만 보내고 응답은 새로 쓴 문장만 받는다. {@code news-draft.writer=openai-api}(기본)일 때 쓴다.
 */
@Slf4j
@ConditionalOnProperty(name = "news-draft.writer", havingValue = "openai-api", matchIfMissing = true)
@Component
public class OpenAiIssueDraftWriter implements IssueDraftWriterPort {

    private static final String BASE_URL = "https://api.openai.com";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    // 추론 모델은 기사 200건을 읽고 초안을 쓰는 데 수 분 걸릴 수 있다.
    private static final Duration READ_TIMEOUT = Duration.ofMinutes(10);

    private final NewsDraftProperties properties;
    private final JsonMapper jsonMapper;
    private final RestClient restClient;

    public OpenAiIssueDraftWriter(NewsDraftProperties properties, JsonMapper jsonMapper) {
        this.properties = properties;
        this.jsonMapper = jsonMapper;
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder().baseUrl(BASE_URL).requestFactory(requestFactory).build();
    }

    @Override
    public List<IssueDraft> write(List<NewsArticle> articles, List<Category> categories, List<String> recentTitles, int maxDrafts) {
        Map<String, Object> request = Map.of(
                "model", properties.model(),
                "instructions", IssueDraftPrompt.INSTRUCTIONS,
                "input", IssueDraftPrompt.input(articles, categories, recentTitles, maxDrafts),
                "text", Map.of("format", Map.of(
                        "type", "json_schema",
                        "name", "issue_drafts",
                        "schema", IssueDraftPrompt.DRAFTS_SCHEMA,
                        "strict", true)));
        String body;
        try {
            body = restClient.post()
                    .uri("/v1/responses")
                    .header("Authorization", "Bearer " + properties.openaiApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonMapper.writeValueAsString(request))
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw NewsDraftException.externalFailure("OpenAI API 호출에 실패했습니다(HTTP " + e.getStatusCode().value() + "): "
                    + errorMessage(e.getResponseBodyAsString()));
        } catch (RestClientException e) {
            throw NewsDraftException.externalFailure("OpenAI API 호출에 실패했습니다: " + e.getMessage());
        }
        return parseDrafts(body);
    }

    List<IssueDraft> parseDrafts(String body) {
        JsonNode response = jsonMapper.readTree(body);
        String status = response.path("status").asString("");
        if ("incomplete".equals(status)) {
            throw NewsDraftException.externalFailure("OpenAI 응답이 끝까지 생성되지 않았습니다: "
                    + response.path("incomplete_details").path("reason").asString("알 수 없음"));
        }
        log.info("뉴스 초안: OpenAI 사용량 input={} output={}",
                response.path("usage").path("input_tokens").asLong(), response.path("usage").path("output_tokens").asLong());

        for (JsonNode output : response.path("output")) {
            if (!"message".equals(output.path("type").asString(""))) {
                continue;
            }
            for (JsonNode content : output.path("content")) {
                String type = content.path("type").asString("");
                if ("refusal".equals(type)) {
                    log.warn("뉴스 초안: OpenAI가 요청을 거절했습니다. {}", content.path("refusal").asString(""));
                    return List.of();
                }
                if ("output_text".equals(type)) {
                    return IssueDraftPrompt.toDrafts(jsonMapper.readTree(content.path("text").asString("{}")));
                }
            }
        }
        throw NewsDraftException.externalFailure("OpenAI 응답에서 초안을 찾지 못했습니다.");
    }

    private String errorMessage(String body) {
        try {
            return jsonMapper.readTree(body).path("error").path("message").asString(body);
        } catch (RuntimeException e) {
            return body;
        }
    }
}
