package com.ilililissue.report.application.service;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.opinion.application.OpinionException;
import com.ilililissue.opinion.application.port.in.ModerateOpinionUseCase;
import com.ilililissue.opinion.domain.entity.Opinion;
import com.ilililissue.opinion.domain.vo.OpinionId;
import com.ilililissue.report.application.ReportException;
import com.ilililissue.report.application.port.in.ReportUseCase;
import com.ilililissue.report.application.port.in.ReportView;
import com.ilililissue.report.application.port.out.ReportPort;
import com.ilililissue.report.domain.entity.Report;
import com.ilililissue.report.domain.vo.ReportId;
import com.ilililissue.report.domain.vo.ReportReason;
import com.ilililissue.report.domain.vo.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ReportService implements ReportUseCase {

    private final ReportPort reportPort;
    private final ModerateOpinionUseCase moderateOpinionUseCase;
    private final Clock clock;

    @Transactional
    @Override
    public Report report(OpinionId opinionId, MemberId reporterId, ReportReason reason) {
        Opinion opinion = moderateOpinionUseCase.getOpinion(opinionId);
        if (opinion.isHidden()) {
            throw OpinionException.notFound();
        }
        if (reportPort.exists(opinionId, reporterId)) {
            throw ReportException.alreadyReported();
        }
        return reportPort.save(Report.receive(opinionId, reporterId, reason, now()));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<ReportView> getReports(ReportStatus status, PageQuery pageQuery) {
        return reportPort.findByStatus(status, pageQuery)
                .map(report -> new ReportView(report, moderateOpinionUseCase.getOpinion(report.getOpinionId())));
    }

    @Transactional
    @Override
    public void hideReportedOpinion(ReportId reportId) {
        Report report = loadReport(reportId);
        report.resolveByHiding(now());
        reportPort.save(report);
        moderateOpinionUseCase.hide(report.getOpinionId());
        for (Report pending : reportPort.findByOpinionIdAndStatus(report.getOpinionId(), ReportStatus.RECEIVED)) {
            pending.resolveByHiding(now());
            reportPort.save(pending);
        }
    }

    @Transactional
    @Override
    public void dismiss(ReportId reportId) {
        Report report = loadReport(reportId);
        report.dismiss(now());
        reportPort.save(report);
    }

    private Report loadReport(ReportId reportId) {
        return reportPort.findById(reportId).orElseThrow(ReportException::notFound);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
