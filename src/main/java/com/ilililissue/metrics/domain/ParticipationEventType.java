package com.ilililissue.metrics.domain;

public enum ParticipationEventType {
    /** 이슈 상세 조회 (비회원 포함) */
    ISSUE_VIEWED,
    /** 투표 */
    VOTED,
    /** 의견 작성 */
    OPINION_WRITTEN,
    /** 입장별 의견 탭 첫 페이지 조회 */
    OPINIONS_VIEWED,
}
