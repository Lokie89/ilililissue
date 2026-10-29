package com.ilililissue.newsdraft.domain;

import java.time.OffsetDateTime;

/**
 * 언론사 피드의 기사 한 건. 제목·요약문은 초안 작성 입력으로만 쓰고 저장하거나 게시하지 않는다.
 *
 * @param publisher   언론사 이름(피드 설정 값)
 * @param url         언론사 원문 URL
 * @param description 피드의 짧은 요약문(기사 본문 아님, 앞부분만)
 */
public record NewsArticle(
        String publisher,
        String title,
        String url,
        String description,
        OffsetDateTime publishedAt
) {
}
