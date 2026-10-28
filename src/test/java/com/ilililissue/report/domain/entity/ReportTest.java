package com.ilililissue.report.domain.entity;

import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.opinion.domain.vo.OpinionId;
import com.ilililissue.report.domain.AlreadyResolvedReportException;
import com.ilililissue.report.domain.vo.ReportReason;
import com.ilililissue.report.domain.vo.ReportStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class ReportTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 28, 9, 0);

    private Report received() {
        return Report.receive(new OpinionId(1), new MemberId(2), ReportReason.from("욕설"), NOW);
    }

    @DisplayName("신고는 접수 상태로 생성된다")
    @Test
    void receive() {
        Assertions.assertThat(received().getStatus()).isEqualTo(ReportStatus.RECEIVED);
    }

    @DisplayName("접수된 신고는 숨김 또는 기각으로 처리한다")
    @Test
    void resolve() {
        Report hidden = received();
        hidden.resolveByHiding(NOW.plusHours(1));
        Assertions.assertThat(hidden.getStatus()).isEqualTo(ReportStatus.RESOLVED_HIDDEN);
        Assertions.assertThat(hidden.getResolvedAt()).isEqualTo(NOW.plusHours(1));

        Report dismissed = received();
        dismissed.dismiss(NOW.plusHours(1));
        Assertions.assertThat(dismissed.getStatus()).isEqualTo(ReportStatus.RESOLVED_DISMISSED);
    }

    @DisplayName("이미 처리된 신고는 다시 처리할 수 없다")
    @Test
    void resolveTwice() {
        Report report = received();
        report.dismiss(NOW);

        Assertions.assertThatThrownBy(() -> report.resolveByHiding(NOW))
                .isInstanceOf(AlreadyResolvedReportException.class);
    }

    @DisplayName("빈 사유, 300자를 넘는 사유는 허용하지 않는다")
    @Test
    void invalidReason() {
        Assertions.assertThatThrownBy(() -> ReportReason.from(" "));
        Assertions.assertThatThrownBy(() -> ReportReason.from("가".repeat(301)));
    }
}
