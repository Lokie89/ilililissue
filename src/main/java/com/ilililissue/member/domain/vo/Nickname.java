package com.ilililissue.member.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class Nickname extends SelfValidating<Nickname> {

    @NotBlank
    @Size(min = 2, max = 12)
    private final String value;

    private Nickname(
            String value
    ) {
        this.value = value;
        this.validate();
    }

    public static Nickname from(String value) {
        return new Nickname(value);
    }
}
