package com.ilililissue.newsdraft.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 뉴스 자동 초안 설정.
 *
 * @param enabled         true면 매일 07:00(Asia/Seoul)에 실행한다. 관리자 수동 실행은 이 값과 무관하다.
 * @param feeds           언론사 면별 RSS 피드. 운영 전에 언론사별 RSS 이용 조건을 확인한다.
 * @param articlesPerFeed 피드마다 쓸 기사 수 상한. 넘으면 24시간에 고르게 나눠 뽑는다
 * @param maxArticles     AI에 넘길 기사 수 상한(중복 제거 후)
 * @param maxDrafts       한 번에 만들 이슈 초안 수 상한
 * @param openaiApiKey    {@code writer=openai-api}일 때 필요한 OpenAI API 키
 * @param model           OpenAI API 모델 ID
 * @param writer          초안 작성 방식. {@code openai-api}(기본, 운영) 또는 {@code codex-cli}(로컬 테스트, ChatGPT 로그인)
 * @param codexCommand    {@code codex-cli}일 때 실행할 명령
 * @param codexModel      {@code codex-cli}일 때 모델. 비우면 Codex 설정(~/.codex/config.toml)의 기본 모델
 */
@ConfigurationProperties("news-draft")
public record NewsDraftProperties(
        boolean enabled,
        List<Feed> feeds,
        int articlesPerFeed,
        int maxArticles,
        int maxDrafts,
        String openaiApiKey,
        String model,
        String writer,
        String codexCommand,
        String codexModel
) {

    public static final String WRITER_OPENAI_API = "openai-api";
    public static final String WRITER_CODEX_CLI = "codex-cli";

    public NewsDraftProperties {
        feeds = feeds == null ? List.of() : feeds.stream()
                .filter(feed -> feed.url() != null && !feed.url().isBlank())
                .toList();
        articlesPerFeed = articlesPerFeed <= 0 ? 12 : articlesPerFeed;
        maxArticles = maxArticles <= 0 ? 400 : maxArticles;
        maxDrafts = maxDrafts <= 0 ? 3 : maxDrafts;
        model = model == null || model.isBlank() ? "gpt-6.1-sol" : model;
        writer = writer == null || writer.isBlank() ? WRITER_OPENAI_API : writer.strip();
        codexCommand = codexCommand == null || codexCommand.isBlank() ? "codex" : codexCommand;
    }

    public boolean hasOpenaiKey() {
        return openaiApiKey != null && !openaiApiKey.isBlank();
    }

    public boolean usesOpenaiApi() {
        return WRITER_OPENAI_API.equals(writer);
    }

    /**
     * @param publisher 출처에 표시할 언론사 이름
     * @param section   면(정치·경제·사회·국제). AI가 카테고리를 고를 때 참고한다
     */
    public record Feed(String publisher, String section, String url) {
    }
}
