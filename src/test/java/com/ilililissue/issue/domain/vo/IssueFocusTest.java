package com.ilililissue.issue.domain.vo;

import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IssueFocusTest {

    @DisplayName("핵심 쟁점과 입장별 근거는 선택이고, 빈 문자열은 없는 것으로 본다")
    @Test
    void optional() {
        IssueFocus focus = IssueFocus.of(" ", null, "");

        Assertions.assertThat(focus.getKeyPoint()).isNull();
        Assertions.assertThat(focus.hasReasons()).isFalse();
        Assertions.assertThat(IssueFocus.of("쟁점", "찬성 근거", "반대 근거").hasReasons()).isTrue();
    }

    @DisplayName("한쪽 입장 근거만 쓸 수 없다")
    @Test
    void balanced() {
        Assertions.assertThatThrownBy(() -> IssueFocus.of("쟁점", "찬성 근거", null))
                .isInstanceOf(ConstraintViolationException.class);
        Assertions.assertThatThrownBy(() -> IssueFocus.of("쟁점", " ", "반대 근거"))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @DisplayName("핵심 쟁점은 100자, 근거는 300자를 넘을 수 없다")
    @Test
    void length() {
        Assertions.assertThatThrownBy(() -> IssueFocus.of("가".repeat(101), null, null))
                .isInstanceOf(ConstraintViolationException.class);
        Assertions.assertThatThrownBy(() -> IssueFocus.of(null, "가".repeat(301), "반대"))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
