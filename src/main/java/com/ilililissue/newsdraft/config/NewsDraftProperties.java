package com.ilililissue.newsdraft.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 뉴스 자동 초안 설정.
 *
 * @param enabled            true면 매일 07:00(Asia/Seoul)에 실행한다. 관리자 수동 실행은 이 값과 무관하다.
 * @param keywords           네이버 뉴스 검색어. 검색 API는 검색어가 필수라 주요 분야 단어로 넓게 모은다.
 * @param articlesPerKeyword 검색어마다 가져올 최신 기사 수(네이버 최대 100)
 * @param maxArticles        Claude에 넘길 기사 수 상한(중복 제거 후)
 * @param maxDrafts          한 번에 만들 이슈 초안 수 상한
 * @param model              Claude 모델 ID
 */
@ConfigurationProperties("news-draft")
public record NewsDraftProperties(
        boolean enabled,
        List<String> keywords,
        int articlesPerKeyword,
        int maxArticles,
        int maxDrafts,
        String naverClientId,
        String naverClientSecret,
        String anthropicApiKey,
        String model
) {

    public NewsDraftProperties {
        keywords = keywords == null ? List.of() : keywords.stream().map(String::strip).filter(k -> !k.isEmpty()).toList();
        articlesPerKeyword = articlesPerKeyword <= 0 ? 30 : Math.min(articlesPerKeyword, 100);
        maxArticles = maxArticles <= 0 ? 200 : maxArticles;
        maxDrafts = maxDrafts <= 0 ? 3 : maxDrafts;
        model = model == null || model.isBlank() ? "claude-opus-5-5" : model;
    }

    public boolean hasNaverKey() {
        return naverClientId != null && !naverClientId.isBlank()
                && naverClientSecret != null && !naverClientSecret.isBlank();
    }

    public boolean hasAnthropicKey() {
        return anthropicApiKey != null && !anthropicApiKey.isBlank();
    }
}
