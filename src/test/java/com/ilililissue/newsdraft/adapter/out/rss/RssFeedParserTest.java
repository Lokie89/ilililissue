package com.ilililissue.newsdraft.adapter.out.rss;

import com.ilililissue.newsdraft.domain.NewsArticle;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

class RssFeedParserTest {

    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    private static List<NewsArticle> parse(String xml) {
        return RssFeedParser.parse(xml.getBytes(StandardCharsets.UTF_8), "예시일보", "사회");
    }

    @DisplayName("RSS 2.0: CDATA·HTML을 걷어내고, 요약문은 앞부분만 쓰며, 여러 날짜 형식과 utm 파라미터를 처리한다")
    @Test
    void rss2() {
        List<NewsArticle> articles = parse("""
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/">
                  <channel>
                    <item>
                      <title><![CDATA[<b>주 4일제</b> &quot;도입&quot; 논의]]></title>
                      <link>https://press.example.com/a/1?utm_source=rss&amp;id=7&amp;utm_medium=feed</link>
                      <description><![CDATA[<p>정부가 &amp; 노동계가</p> <img src="x.jpg"/>논의]]></description>
                      <pubDate>Wed, 28 Oct 2026 09:30:00 +0900</pubDate>
                    </item>
                    <item>
                      <title>콜론 오프셋</title>
                      <link>https://press.example.com/a/2</link>
                      <description>%s</description>
                      <pubDate>Wed, 28 Oct 2026 09:30:00 +09:00</pubDate>
                    </item>
                    <item>
                      <title>dc:date</title>
                      <link>https://press.example.com/a/3?utm_source=rss</link>
                      <dc:date>2026-10-28T09:30:00+09:00</dc:date>
                    </item>
                    <item>
                      <title>시각 없음</title>
                      <link>https://press.example.com/a/4</link>
                    </item>
                  </channel>
                </rss>
                """.formatted("가".repeat(300)));

        OffsetDateTime expected = OffsetDateTime.of(2026, 10, 28, 9, 30, 0, 0, KST);
        Assertions.assertThat(articles).hasSize(3);
        Assertions.assertThat(articles.get(0)).isEqualTo(new NewsArticle(
                "예시일보", "사회", "주 4일제 \"도입\" 논의", "https://press.example.com/a/1?id=7", "정부가 & 노동계가 논의", expected));
        Assertions.assertThat(articles.get(1).publishedAt()).isEqualTo(expected);
        Assertions.assertThat(articles.get(1).description()).hasSize(RssFeedParser.DESCRIPTION_MAX_LENGTH + 1).endsWith("…");
        Assertions.assertThat(articles.get(2).url()).isEqualTo("https://press.example.com/a/3");
        Assertions.assertThat(articles.get(2).publishedAt()).isEqualTo(expected);
    }

    @DisplayName("항목에 시각이 없으면 피드의 lastBuildDate를 쓴다")
    @Test
    void fallbackToLastBuildDate() {
        List<NewsArticle> articles = parse("""
                <rss version="2.0"><channel>
                  <lastBuildDate>Thu, 29 Oct 2026 02:07:09 GMT</lastBuildDate>
                  <item><title>제목</title><link>https://press.example.com/a/1</link></item>
                </channel></rss>
                """);

        Assertions.assertThat(articles).singleElement()
                .extracting(NewsArticle::publishedAt)
                .isEqualTo(OffsetDateTime.of(2026, 10, 29, 2, 7, 9, 0, ZoneOffset.UTC));
    }

    @DisplayName("Atom 피드의 entry도 읽는다")
    @Test
    void atom() {
        List<NewsArticle> articles = parse("""
                <feed xmlns="http://www.w3.org/2005/Atom">
                  <entry>
                    <title>아톰 기사</title>
                    <link href="https://press.example.com/atom/1"/>
                    <summary>요약</summary>
                    <updated>2026-10-28T00:30:00Z</updated>
                  </entry>
                </feed>
                """);

        Assertions.assertThat(articles).singleElement().satisfies(article -> {
            Assertions.assertThat(article.url()).isEqualTo("https://press.example.com/atom/1");
            Assertions.assertThat(article.description()).isEqualTo("요약");
        });
    }

    @DisplayName("DOCTYPE이 있는 문서(봇 차단 HTML, 외부 엔티티)는 읽지 않는다")
    @Test
    void rejectsDoctype() {
        Assertions.assertThatThrownBy(() -> parse("<!DOCTYPE html><html><head><title>Just a moment...</title></head></html>"))
                .isInstanceOf(IllegalStateException.class);
        Assertions.assertThatThrownBy(() -> parse("""
                        <?xml version="1.0"?>
                        <!DOCTYPE rss [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                        <rss><channel><item><title>&xxe;</title></item></channel></rss>
                        """))
                .isInstanceOf(IllegalStateException.class);
    }
}
