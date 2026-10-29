package com.ilililissue.newsdraft.domain;

import java.util.List;

/**
 * AI가 제안한 이슈 초안. 서비스가 카테고리·출처 URL을 확인한 뒤 DRAFT 이슈로 등록한다.
 *
 * @param title          찬성·중립·반대로 답할 수 있는 중립 질문
 * @param summary        기사 표현을 옮기지 않고 새로 쓴 사실관계 배경 설명
 * @param categoryId     기존 카테고리 중 하나
 * @param sourceUrls     근거 기사 URL. 수집한 기사 URL과 같을 때만 출처로 쓴다
 * @param keyPoint       핵심 쟁점 한 줄
 * @param agreeReason    찬성 측 대표 근거
 * @param disagreeReason 반대·신중 측 대표 근거
 */
public record IssueDraft(
        String title,
        String summary,
        long categoryId,
        List<String> sourceUrls,
        String keyPoint,
        String agreeReason,
        String disagreeReason
) {
}
