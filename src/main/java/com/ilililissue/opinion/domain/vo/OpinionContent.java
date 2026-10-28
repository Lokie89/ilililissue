package com.ilililissue.opinion.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class OpinionContent extends SelfValidating<OpinionContent> {

    @NotBlank
    @Size(max = 500)
    private final String value;

    private OpinionContent(
            String value
    ) {
        this.value = value;
        this.validate();
    }

    public static OpinionContent from(String value) {
        return new OpinionContent(value);
    }
}
