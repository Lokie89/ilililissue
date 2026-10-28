package com.ilililissue.report.adapter.out.persistence;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.opinion.domain.vo.OpinionId;
import com.ilililissue.report.application.port.out.ReportPort;
import com.ilililissue.report.domain.entity.Report;
import com.ilililissue.report.domain.vo.ReportId;
import com.ilililissue.report.domain.vo.ReportReason;
import com.ilililissue.report.domain.vo.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ReportPersistenceAdapter implements ReportPort {

    private final ReportJpaRepository reportJpaRepository;

    @Override
    public Report save(Report report) {
        return mapToDomain(reportJpaRepository.save(new ReportJpaEntity(
                report.getId() == null ? null : report.getId().getValue(),
                report.getOpinionId().getValue(),
                report.getReporterId().getValue(),
                report.getReason().getValue(),
                report.getStatus(),
                report.getReportedAt(),
                report.getResolvedAt()
        )));
    }

    @Override
    public Optional<Report> findById(ReportId id) {
        return reportJpaRepository.findById(id.getValue()).map(this::mapToDomain);
    }

    @Override
    public boolean exists(OpinionId opinionId, MemberId reporterId) {
        return reportJpaRepository.existsByOpinionIdAndReporterId(opinionId.getValue(), reporterId.getValue());
    }

    @Override
    public List<Report> findByOpinionIdAndStatus(OpinionId opinionId, ReportStatus status) {
        return reportJpaRepository.findByOpinionIdAndStatus(opinionId.getValue(), status).stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public PageResult<Report> findByStatus(ReportStatus status, PageQuery pageQuery) {
        Page<ReportJpaEntity> page = reportJpaRepository.findByStatus(status,
                PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by(Sort.Order.asc("reportedAt"),
                        Sort.Order.asc("id"))));
        return new PageResult<>(page.getContent().stream().map(this::mapToDomain).toList(),
                pageQuery.page(), pageQuery.size(), page.getTotalElements());
    }

    private Report mapToDomain(ReportJpaEntity entity) {
        return Report.withId(
                new ReportId(entity.getId()),
                new OpinionId(entity.getOpinionId()),
                new MemberId(entity.getReporterId()),
                ReportReason.from(entity.getReason()),
                entity.getStatus(),
                entity.getReportedAt(),
                entity.getResolvedAt()
        );
    }
}
