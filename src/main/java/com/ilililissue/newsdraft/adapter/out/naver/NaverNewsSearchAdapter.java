package com.ilililissue.newsdraft.adapter.out.naver;

import com.ilililissue.newsdraft.application.port.out.NewsSearchPort;
import com.ilililissue.newsdraft.config.NewsDraftProperties;
import com.ilililissue.newsdraft.domain.NewsArticle;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.HtmlUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

/**
 * 네이버 검색 API(뉴스). 인터페이스: {@code GET https://openapi.naver.com/v1/search/news.json?query&display&sort=date},
 * 헤더 {@code X-Naver-Client-Id}, {@code X-Naver-Client-Secret}. 하루 호출 한도 25,000회.
 * 응답 제목·요약문에는 검색어 강조 태그({@code <b>})와 HTML 엔티티가 섞여 있어 걷어낸다.
 */
@Component
public class NaverNewsSearchAdapter implements NewsSearchPort {

    private static final String BASE_URL = "https://openapi.naver.com";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final NewsDraftProperties properties;
    private final RestClient restClient;

    public NaverNewsSearchAdapter(NewsDraftProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT);
        requestFactory.setReadTimeout(TIMEOUT);
        this.restClient = RestClient.builder().baseUrl(BASE_URL).requestFactory(requestFactory).build();
    }

    @Override
    public List<NewsArticle> searchLatest(String keyword, int count) {
        NaverNewsResponse response = restClient.get()
                .uri(uri -> uri.path("/v1/search/news.json")
                        .queryParam("query", keyword)
                        .queryParam("display", count)
                        .queryParam("sort", "date")
                        .build())
                .header("X-Naver-Client-Id", properties.naverClientId())
                .header("X-Naver-Client-Secret", properties.naverClientSecret())
                .retrieve()
                .body(NaverNewsResponse.class);
        if (response == null || response.items() == null) {
            return List.of();
        }
        return response.items().stream().map(NaverNewsSearchAdapter::toArticle).filter(Objects::nonNull).toList();
    }

    static NewsArticle toArticle(NaverNewsItem item) {
        String url = item.originallink() != null && !item.originallink().isBlank() ? item.originallink() : item.link();
        OffsetDateTime publishedAt = parsePubDate(item.pubDate());
        if (url == null || url.isBlank() || publishedAt == null) {
            return null;
        }
        return new NewsArticle(plainText(item.title()), url.strip(), plainText(item.description()), publishedAt);
    }

    static String plainText(String html) {
        if (html == null) {
            return "";
        }
        return HtmlUtils.htmlUnescape(html.replaceAll("<[^>]+>", "")).strip();
    }

    /**
     * 예: {@code Mon, 26 Sep 2016 07:50:00 +0900}
     */
    static OffsetDateTime parsePubDate(String pubDate) {
        if (pubDate == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(pubDate.strip(), DateTimeFormatter.RFC_1123_DATE_TIME);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    record NaverNewsResponse(List<NaverNewsItem> items) {
    }

    record NaverNewsItem(String title, String originallink, String link, String description, String pubDate) {
    }
}
