package com.ilililissue.report.application.port.in;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.opinion.domain.vo.OpinionId;
import com.ilililissue.report.domain.entity.Report;
import com.ilililissue.report.domain.vo.ReportId;
import com.ilililissue.report.domain.vo.ReportReason;
import com.ilililissue.report.domain.vo.ReportStatus;

public interface ReportUseCase {

    Report report(OpinionId opinionId, MemberId reporterId, ReportReason reason);

    PageResult<ReportView> getReports(ReportStatus status, PageQuery pageQuery);

    /**
     * 신고된 의견을 숨기고, 같은 의견의 접수 상태 신고를 모두 숨김 처리로 종결한다.
     */
    void hideReportedOpinion(ReportId reportId);

    void dismiss(ReportId reportId);
}
