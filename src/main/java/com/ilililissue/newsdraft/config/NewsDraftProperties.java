package com.ilililissue.newsdraft.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 뉴스 자동 초안 설정.
 *
 * @param enabled         true면 매일 07:00(Asia/Seoul)에 실행한다. 관리자 수동 실행은 이 값과 무관하다.
 * @param feeds           언론사 RSS 피드. 운영 전에 언론사별 RSS 이용 조건을 확인한다.
 * @param articlesPerFeed 피드마다 쓸 최신 기사 수 상한
 * @param maxArticles     AI에 넘길 기사 수 상한(중복 제거 후)
 * @param maxDrafts       한 번에 만들 이슈 초안 수 상한
 * @param model           OpenAI 모델 ID
 */
@ConfigurationProperties("news-draft")
public record NewsDraftProperties(
        boolean enabled,
        List<Feed> feeds,
        int articlesPerFeed,
        int maxArticles,
        int maxDrafts,
        String openaiApiKey,
        String model
) {

    public NewsDraftProperties {
        feeds = feeds == null ? List.of() : feeds.stream()
                .filter(feed -> feed.url() != null && !feed.url().isBlank())
                .toList();
        articlesPerFeed = articlesPerFeed <= 0 ? 40 : articlesPerFeed;
        maxArticles = maxArticles <= 0 ? 200 : maxArticles;
        maxDrafts = maxDrafts <= 0 ? 3 : maxDrafts;
        model = model == null || model.isBlank() ? "gpt-6.1-sol" : model;
    }

    public boolean hasOpenaiKey() {
        return openaiApiKey != null && !openaiApiKey.isBlank();
    }

    /**
     * @param publisher 출처에 표시할 언론사 이름
     */
    public record Feed(String publisher, String url) {
    }
}
