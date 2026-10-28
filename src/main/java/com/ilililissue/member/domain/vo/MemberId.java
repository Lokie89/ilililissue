package com.ilililissue.member.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class MemberId extends SelfValidating<MemberId> {

    @Min(1)
    private final long value;

    public MemberId(
            long value
    ) {
        this.value = value;
        this.validate();
    }
}
