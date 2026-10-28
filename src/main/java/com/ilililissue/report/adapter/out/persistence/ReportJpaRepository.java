package com.ilililissue.report.adapter.out.persistence;

import com.ilililissue.report.domain.vo.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportJpaRepository extends JpaRepository<ReportJpaEntity, Long> {

    boolean existsByOpinionIdAndReporterId(Long opinionId, Long reporterId);

    List<ReportJpaEntity> findByOpinionIdAndStatus(Long opinionId, ReportStatus status);

    Page<ReportJpaEntity> findByStatus(ReportStatus status, Pageable pageable);
}
