package com.ilililissue.issue.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 관리자가 자체 작성한 중립적인 배경 설명. 기사 본문을 복사하지 않는다.
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class IssueSummary extends SelfValidating<IssueSummary> {

    @NotBlank
    @Size(max = 1000)
    private final String value;

    private IssueSummary(
            String value
    ) {
        this.value = value;
        this.validate();
    }

    public static IssueSummary from(String value) {
        return new IssueSummary(value);
    }
}
