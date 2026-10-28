package com.ilililissue.member.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class MemberBlockId extends SelfValidating<MemberBlockId> {

    @Min(1)
    private final long value;

    public MemberBlockId(
            long value
    ) {
        this.value = value;
        this.validate();
    }
}
