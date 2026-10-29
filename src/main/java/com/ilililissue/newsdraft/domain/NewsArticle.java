package com.ilililissue.newsdraft.domain;

import java.time.OffsetDateTime;

/**
 * 뉴스 검색 결과 한 건. 제목·요약문은 초안 작성 입력으로만 쓰고 저장하거나 게시하지 않는다.
 *
 * @param url         언론사 원문 URL
 * @param description 검색 결과의 짧은 요약문(기사 본문 아님)
 */
public record NewsArticle(
        String title,
        String url,
        String description,
        OffsetDateTime publishedAt
) {
}
