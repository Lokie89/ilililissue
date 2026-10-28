package com.ilililissue.metrics.application.service;

import com.ilililissue.metrics.application.port.in.ParticipationMetrics;
import com.ilililissue.metrics.application.port.out.ParticipationEventPort;
import com.ilililissue.metrics.application.port.out.ParticipationEventPort.MemberActivity;
import com.ilililissue.metrics.domain.ParticipationEventType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MetricsServiceTest {

    private static final LocalDate FROM = LocalDate.of(2026, 11, 1);
    private static final LocalDate TO = LocalDate.of(2026, 11, 28);

    @Mock
    ParticipationEventPort eventPort;

    private LocalDateTime day(int dayOfMonth) {
        return LocalDate.of(2026, 11, dayOfMonth).atTime(12, 0);
    }

    @DisplayName("비율은 분모 대비 백분율이고 분모가 0이면 0이다")
    @Test
    void rates() {
        when(eventPort.count(eq(ParticipationEventType.ISSUE_VIEWED), any(), any())).thenReturn(320L);
        when(eventPort.countDistinctIssueVisitors(any(), any())).thenReturn(200L);
        when(eventPort.count(eq(ParticipationEventType.VOTED), any(), any())).thenReturn(50L);
        when(eventPort.count(eq(ParticipationEventType.OPINION_WRITTEN), any(), any())).thenReturn(8L);
        when(eventPort.countDistinctMembers(eq(ParticipationEventType.VOTED), any(), any())).thenReturn(40L);
        when(eventPort.countDistinctOpposingViewers(any(), any())).thenReturn(12L);
        when(eventPort.findMemberActivities(any())).thenReturn(List.of());

        ParticipationMetrics metrics = new MetricsService(eventPort, Clock.systemDefaultZone()).getMetrics(FROM, TO);

        Assertions.assertThat(metrics.voteRate()).isEqualTo(25.0);
        Assertions.assertThat(metrics.opinionRate()).isEqualTo(16.0);
        Assertions.assertThat(metrics.opposingViewRate()).isEqualTo(30.0);
        Assertions.assertThat(metrics.revisit7dRate()).isEqualTo(0.0);
    }

    @DisplayName("7일 재방문율: 기간 내 처음 활동하고 7일 이상 지나 다시 활동한 회원 비율")
    @Test
    void revisitRate() {
        when(eventPort.findMemberActivities(any())).thenReturn(List.of(
                new MemberActivity(1, day(1), day(8)),            // 7일 후 재방문
                new MemberActivity(2, day(2), day(5)),            // 재방문 없음
                new MemberActivity(3, day(3), day(20)),           // 재방문
                new MemberActivity(4, day(1).minusDays(10), day(10)), // 기간 전 첫 활동 → 제외
                new MemberActivity(5, day(25), day(28))           // 7일이 지나지 않음 → 제외
        ));

        ParticipationMetrics metrics = new MetricsService(eventPort, Clock.systemDefaultZone()).getMetrics(FROM, TO);

        Assertions.assertThat(metrics.revisitCohort()).isEqualTo(3);
        Assertions.assertThat(metrics.revisit7dRate()).isEqualTo(66.7);
    }
}
