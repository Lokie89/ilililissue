package com.ilililissue.newsdraft.adapter.out.naver;

import com.ilililissue.newsdraft.adapter.out.naver.NaverNewsSearchAdapter.NaverNewsItem;
import com.ilililissue.newsdraft.domain.NewsArticle;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

class NaverNewsSearchAdapterTest {

    @DisplayName("강조 태그와 HTML 엔티티를 걷어내고, 언론사 원문 링크와 발행 시각을 읽는다")
    @Test
    void toArticle() {
        NewsArticle article = NaverNewsSearchAdapter.toArticle(new NaverNewsItem(
                "<b>주 4일제</b> &quot;도입&quot; 논의",
                "https://press.example.com/a/1",
                "https://n.news.naver.com/article/1",
                "정부가 &amp; 노동계가 <b>논의</b>",
                "Wed, 28 Oct 2026 09:30:00 +0900"));

        Assertions.assertThat(article.title()).isEqualTo("주 4일제 \"도입\" 논의");
        Assertions.assertThat(article.description()).isEqualTo("정부가 & 노동계가 논의");
        Assertions.assertThat(article.url()).isEqualTo("https://press.example.com/a/1");
        Assertions.assertThat(article.publishedAt())
                .isEqualTo(OffsetDateTime.of(2026, 10, 28, 9, 30, 0, 0, ZoneOffset.ofHours(9)));
    }

    @DisplayName("원문 링크가 없으면 네이버 링크를 쓰고, 발행 시각을 읽을 수 없으면 버린다")
    @Test
    void fallbackAndInvalid() {
        Assertions.assertThat(NaverNewsSearchAdapter.toArticle(new NaverNewsItem(
                        "제목", "", "https://n.news.naver.com/article/1", "요약", "Wed, 28 Oct 2026 09:30:00 +0900")).url())
                .isEqualTo("https://n.news.naver.com/article/1");
        Assertions.assertThat(NaverNewsSearchAdapter.toArticle(new NaverNewsItem(
                "제목", "https://press.example.com/a/1", null, "요약", "어제"))).isNull();
    }
}
