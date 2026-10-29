package com.ilililissue.newsdraft.domain;

import java.util.List;

/**
 * AI가 제안한 이슈 초안. 서비스가 카테고리·출처 URL을 확인한 뒤 DRAFT 이슈로 등록한다.
 *
 * @param title      찬성·중립·반대로 답할 수 있는 중립 질문
 * @param summary    기사 표현을 옮기지 않고 새로 쓴 배경 설명
 * @param categoryId 기존 카테고리 중 하나
 * @param sources    근거 기사. URL은 수집한 기사 중에서만 고른다
 */
public record IssueDraft(
        String title,
        String summary,
        long categoryId,
        List<Source> sources
) {

    /**
     * @param publisher 언론사 이름(URL 도메인으로 추정)
     */
    public record Source(String url, String publisher) {
    }
}
