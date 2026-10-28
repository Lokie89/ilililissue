package com.ilililissue.issue.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class CategoryId extends SelfValidating<CategoryId> {

    @Min(1)
    private final long value;

    public CategoryId(
            long value
    ) {
        this.value = value;
        this.validate();
    }
}
