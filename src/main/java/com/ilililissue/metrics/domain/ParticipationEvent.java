package com.ilililissue.metrics.domain;

import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.vote.domain.vo.Stance;

import java.time.LocalDateTime;

/**
 * 참여 지표 측정용 이벤트.
 *
 * @param memberId    비회원이면 null
 * @param stance      회원의 투표 입장 (투표 전이거나 비회원이면 null)
 * @param targetStance 열람한 의견 탭의 입장 ({@link ParticipationEventType#OPINIONS_VIEWED}에서만 사용)
 */
public record ParticipationEvent(
        ParticipationEventType type,
        IssueId issueId,
        MemberId memberId,
        Stance stance,
        Stance targetStance,
        LocalDateTime occurredAt
) {
}
