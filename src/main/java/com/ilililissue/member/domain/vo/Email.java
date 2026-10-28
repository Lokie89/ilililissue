package com.ilililissue.member.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Locale;

@Getter
@EqualsAndHashCode(callSuper = false)
public class Email extends SelfValidating<Email> {

    @NotBlank
    @jakarta.validation.constraints.Email
    private final String value;

    private Email(
            String value
    ) {
        this.value = value == null ? null : value.trim().toLowerCase(Locale.ROOT);
        this.validate();
    }

    public static Email from(String value) {
        return new Email(value);
    }
}
