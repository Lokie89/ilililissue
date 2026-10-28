package com.ilililissue.report.application.port.out;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.opinion.domain.vo.OpinionId;
import com.ilililissue.report.domain.entity.Report;
import com.ilililissue.report.domain.vo.ReportId;
import com.ilililissue.report.domain.vo.ReportStatus;

import java.util.List;
import java.util.Optional;

public interface ReportPort {

    Report save(Report report);

    Optional<Report> findById(ReportId id);

    boolean exists(OpinionId opinionId, MemberId reporterId);

    List<Report> findByOpinionIdAndStatus(OpinionId opinionId, ReportStatus status);

    PageResult<Report> findByStatus(ReportStatus status, PageQuery pageQuery);
}
