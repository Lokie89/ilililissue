package com.ilililissue.report.adapter.in.web;

import com.ilililissue.common.adapter.in.web.LoginMember;
import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.opinion.application.port.in.ModerateOpinionUseCase;
import com.ilililissue.opinion.domain.entity.Opinion;
import com.ilililissue.opinion.domain.vo.OpinionId;
import com.ilililissue.report.application.port.in.ReportUseCase;
import com.ilililissue.report.application.port.in.ReportView;
import com.ilililissue.report.domain.entity.Report;
import com.ilililissue.report.domain.vo.ReportId;
import com.ilililissue.report.domain.vo.ReportReason;
import com.ilililissue.report.domain.vo.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
public class ReportController {

    private final ReportUseCase reportUseCase;
    private final ModerateOpinionUseCase moderateOpinionUseCase;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/opinions/{opinionId}/reports")
    public ReportResponse report(@PathVariable long opinionId, @LoginMember MemberId memberId,
                                 @RequestBody ReportRequest request) {
        return ReportResponse.from(
                reportUseCase.report(new OpinionId(opinionId), memberId, ReportReason.from(request.reason())));
    }

    @GetMapping("/api/admin/reports")
    public PageResult<AdminReportResponse> reports(@RequestParam(defaultValue = "RECEIVED") ReportStatus status,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return reportUseCase.getReports(status, PageQuery.of(page, size)).map(AdminReportResponse::from);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/api/admin/reports/{reportId}/hide")
    public void hide(@PathVariable long reportId) {
        reportUseCase.hideReportedOpinion(new ReportId(reportId));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/api/admin/reports/{reportId}/dismiss")
    public void dismiss(@PathVariable long reportId) {
        reportUseCase.dismiss(new ReportId(reportId));
    }

    /**
     * 신고 없이 관리자가 직접 의견을 숨긴다.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/api/admin/opinions/{opinionId}/hide")
    public void hideOpinion(@PathVariable long opinionId) {
        moderateOpinionUseCase.hide(new OpinionId(opinionId));
    }

    public record ReportRequest(String reason) {
    }

    public record ReportResponse(long id, long opinionId, ReportStatus status, LocalDateTime reportedAt) {

        static ReportResponse from(Report report) {
            return new ReportResponse(report.getId().getValue(), report.getOpinionId().getValue(),
                    report.getStatus(), report.getReportedAt());
        }
    }

    public record AdminReportResponse(long id, long opinionId, long issueId, String opinionContent,
                                      boolean opinionHidden, long reporterId, String reason, ReportStatus status,
                                      LocalDateTime reportedAt, LocalDateTime resolvedAt) {

        static AdminReportResponse from(ReportView view) {
            Report report = view.report();
            Opinion opinion = view.opinion();
            return new AdminReportResponse(
                    report.getId().getValue(),
                    opinion.getId().getValue(),
                    opinion.getIssueId().getValue(),
                    opinion.getContent().getValue(),
                    opinion.isHidden(),
                    report.getReporterId().getValue(),
                    report.getReason().getValue(),
                    report.getStatus(),
                    report.getReportedAt(),
                    report.getResolvedAt()
            );
        }
    }
}
