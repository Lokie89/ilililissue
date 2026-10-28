package com.ilililissue.metrics.application.port.out;

import com.ilililissue.metrics.domain.ParticipationEvent;
import com.ilililissue.metrics.domain.ParticipationEventType;

import java.time.LocalDateTime;
import java.util.List;

public interface ParticipationEventPort {

    void save(ParticipationEvent event);

    long count(ParticipationEventType type, LocalDateTime from, LocalDateTime to);

    /**
     * (이슈, 방문자) 쌍의 수. 방문자 식별자가 없는 조회는 세지 않는다.
     */
    long countDistinctIssueVisitors(LocalDateTime from, LocalDateTime to);

    long countDistinctMembers(ParticipationEventType type, LocalDateTime from, LocalDateTime to);

    /**
     * 자신의 투표 입장과 다른 입장의 의견 탭을 연 회원 수 (중복 제외).
     */
    long countDistinctOpposingViewers(LocalDateTime from, LocalDateTime to);

    /**
     * 회원별 첫 활동·마지막 활동 시각. 기간 끝까지의 전체 활동을 기준으로 한다.
     */
    List<MemberActivity> findMemberActivities(LocalDateTime until);

    record MemberActivity(long memberId, LocalDateTime firstAt, LocalDateTime lastAt) {
    }
}
