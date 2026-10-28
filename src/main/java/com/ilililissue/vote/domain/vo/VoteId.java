package com.ilililissue.vote.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class VoteId extends SelfValidating<VoteId> {

    @Min(1)
    private final long value;

    public VoteId(
            long value
    ) {
        this.value = value;
        this.validate();
    }
}
