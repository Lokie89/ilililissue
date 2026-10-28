package com.ilililissue.issue.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class IssueId extends SelfValidating<IssueId> {

    @Min(1)
    private final long value;

    public IssueId(
            long value
    ) {
        this.value = value;
        this.validate();
    }
}
