package com.ilililissue.newsdraft.adapter.out.rss;

import com.ilililissue.newsdraft.application.port.out.NewsFeedPort;
import com.ilililissue.newsdraft.config.NewsDraftProperties.Feed;
import com.ilililissue.newsdraft.domain.NewsArticle;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

/**
 * 언론사 RSS 피드. 인터페이스: {@code GET <피드 URL>}(인증 없음), 응답 RSS 2.0·RSS 1.0·Atom XML.
 * 하루 한 번 피드당 1회만 요청하고, 봇 차단(Cloudflare 등)은 우회하지 않는다.
 */
@Component
public class RssNewsFeedAdapter implements NewsFeedPort {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String USER_AGENT = "ilililissue-news-draft/1.0";

    private final RestClient restClient;

    public RssNewsFeedAdapter() {
        // 피드 주소가 308 등으로 옮겨진 경우를 따라간다(https→http로 내려가는 이동은 따르지 않음).
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(TIMEOUT);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    @Override
    public List<NewsArticle> fetch(Feed feed) {
        byte[] body = restClient.get()
                .uri(feed.url())
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml")
                .retrieve()
                .body(byte[].class);
        if (body == null || body.length == 0) {
            return List.of();
        }
        return RssFeedParser.parse(body, feed.publisher());
    }
}
