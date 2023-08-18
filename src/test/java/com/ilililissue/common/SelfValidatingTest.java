package com.ilililissue.common;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
public class SelfValidatingTest {

    private class ValidateClass extends SelfValidating<ValidateClass> {

        @NotNull
        private final String notNullString;

        @Min(50)
        private final int minimum50;

        private ValidateClass(
                String notNullString,
                int number
        ) {
            this.notNullString = notNullString;
            this.minimum50 = number;
            this.validate();
        }
    }

    @DisplayName("셀프 검증 테스트")
    @Test
    void validate() {
        ValidateClass pass = new ValidateClass("Test", 60);
        Assertions.assertThatThrownBy(() -> new ValidateClass(null, 60));
        Assertions.assertThatThrownBy(() -> new ValidateClass("test", 40));
    }

}
