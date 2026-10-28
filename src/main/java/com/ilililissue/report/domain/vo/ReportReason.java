package com.ilililissue.report.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 신고 사유. 사유 목록이 정해지기 전까지 자유 입력으로 받는다.
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class ReportReason extends SelfValidating<ReportReason> {

    @NotBlank
    @Size(max = 300)
    private final String value;

    private ReportReason(
            String value
    ) {
        this.value = value;
        this.validate();
    }

    public static ReportReason from(String value) {
        return new ReportReason(value);
    }
}
