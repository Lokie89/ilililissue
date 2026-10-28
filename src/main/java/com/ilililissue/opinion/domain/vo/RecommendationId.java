package com.ilililissue.opinion.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class RecommendationId extends SelfValidating<RecommendationId> {

    @Min(1)
    private final long value;

    public RecommendationId(
            long value
    ) {
        this.value = value;
        this.validate();
    }
}
