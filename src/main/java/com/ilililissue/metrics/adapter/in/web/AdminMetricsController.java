package com.ilililissue.metrics.adapter.in.web;

import com.ilililissue.metrics.application.port.in.MetricsQuery;
import com.ilililissue.metrics.application.port.in.ParticipationMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
public class AdminMetricsController {

    private final MetricsQuery metricsQuery;

    @GetMapping("/api/admin/metrics")
    public ParticipationMetrics metrics(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return metricsQuery.getMetrics(from, to);
    }
}
