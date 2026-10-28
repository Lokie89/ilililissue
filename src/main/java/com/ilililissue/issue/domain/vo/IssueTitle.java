package com.ilililissue.issue.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class IssueTitle extends SelfValidating<IssueTitle> {

    @NotBlank
    @Size(max = 100)
    private final String value;

    private IssueTitle(
            String value
    ) {
        this.value = value;
        this.validate();
    }

    public static IssueTitle from(String value) {
        return new IssueTitle(value);
    }
}
