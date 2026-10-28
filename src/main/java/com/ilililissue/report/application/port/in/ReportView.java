package com.ilililissue.report.application.port.in;

import com.ilililissue.opinion.domain.entity.Opinion;
import com.ilililissue.report.domain.entity.Report;

/**
 * 관리자 신고 목록 항목. 판단을 위해 신고된 의견 내용을 함께 보여준다.
 */
public record ReportView(Report report, Opinion opinion) {
}
