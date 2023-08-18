package com.ilililissue.comment.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
@EqualsAndHashCode(callSuper = false)
public class Id extends SelfValidating<Id> {

    @Min(1)
    private final long value;

    public Id(
            long value
    ) {
        this.value = value;
        this.validate();
    }

}
