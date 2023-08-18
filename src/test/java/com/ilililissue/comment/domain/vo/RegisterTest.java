package com.ilililissue.comment.domain.vo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
public class RegisterTest {

    @DisplayName("댓글 생성자 동치성 테스트")
    @Test
    void equals() {

        final Id originId = new Id(5);
        final String originUsername = "ENTJ";
        Register originRegister = Register.of(originId, originUsername);

        Register sameIdRegister = Register.of(originId, originUsername);
        Assertions.assertThat(originRegister).isEqualTo(sameIdRegister);

        final Id sameValueId = new Id(5);

        Register sameValueIdRegister = Register.of(sameValueId, originUsername);
        Assertions.assertThat(originRegister).isEqualTo(sameValueIdRegister);

        final String differentUsername = "INFP";

        Register sameValueIdDifferentUsernameRegister = Register.of(sameValueId, differentUsername);
        Assertions.assertThat(originRegister).isEqualTo(sameValueIdDifferentUsernameRegister);

        final Id differentValueId = new Id(6);

        Register differentValueIdSameUsernameRegister = Register.of(differentValueId, originUsername);
        Assertions.assertThat(originRegister).isNotEqualTo(differentValueIdSameUsernameRegister);

    }
}
