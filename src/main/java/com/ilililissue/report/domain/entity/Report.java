package com.ilililissue.report.domain.entity;

import com.ilililissue.common.SelfValidating;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.opinion.domain.vo.OpinionId;
import com.ilililissue.report.domain.AlreadyResolvedReportException;
import com.ilililissue.report.domain.vo.ReportId;
import com.ilililissue.report.domain.vo.ReportReason;
import com.ilililissue.report.domain.vo.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * <h2>신고 도메인</h2>
 * <ul>
 * <li>회원당 의견별 1회이며 저장소의 (opinionId, reporterId) 유니크 제약으로 보장한다.</li>
 * <li>접수(RECEIVED) 상태에서만 숨김 또는 기각으로 처리할 수 있다.</li>
 * <li>숨김 처리 시 의견 숨김은 application 계층에서 함께 수행한다.</li>
 * </ul>
 */
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
public class Report extends SelfValidating<Report> {

    private final ReportId id;

    @NotNull
    private final OpinionId opinionId;

    @NotNull
    private final MemberId reporterId;

    @NotNull
    private final ReportReason reason;

    @NotNull
    private ReportStatus status;

    @NotNull
    private final LocalDateTime reportedAt;

    private LocalDateTime resolvedAt;

    private Report(
            ReportId id,
            OpinionId opinionId,
            MemberId reporterId,
            ReportReason reason,
            ReportStatus status,
            LocalDateTime reportedAt,
            LocalDateTime resolvedAt
    ) {
        this.id = id;
        this.opinionId = opinionId;
        this.reporterId = reporterId;
        this.reason = reason;
        this.status = status;
        this.reportedAt = reportedAt;
        this.resolvedAt = resolvedAt;
        this.validate();
    }

    public static Report receive(OpinionId opinionId, MemberId reporterId, ReportReason reason, LocalDateTime now) {
        return new Report(null, opinionId, reporterId, reason, ReportStatus.RECEIVED, now, null);
    }

    public static Report withId(ReportId id, OpinionId opinionId, MemberId reporterId, ReportReason reason,
                                ReportStatus status, LocalDateTime reportedAt, LocalDateTime resolvedAt) {
        return new Report(id, opinionId, reporterId, reason, status, reportedAt, resolvedAt);
    }

    public void resolveByHiding(LocalDateTime now) {
        resolve(ReportStatus.RESOLVED_HIDDEN, now);
    }

    public void dismiss(LocalDateTime now) {
        resolve(ReportStatus.RESOLVED_DISMISSED, now);
    }

    private void resolve(ReportStatus resolvedStatus, LocalDateTime now) {
        if (this.status != ReportStatus.RECEIVED) {
            throw new AlreadyResolvedReportException();
        }
        this.status = resolvedStatus;
        this.resolvedAt = now;
    }
}
