package com.ilililissue.report.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class ReportId extends SelfValidating<ReportId> {

    @Min(1)
    private final long value;

    public ReportId(
            long value
    ) {
        this.value = value;
        this.validate();
    }
}
