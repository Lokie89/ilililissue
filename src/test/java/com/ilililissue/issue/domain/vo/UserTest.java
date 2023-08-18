package com.ilililissue.issue.domain.vo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
public class UserTest {

    @DisplayName("이슈 유저 동치성 테스트")
    @Test
    void equals() {

        final UserRole administratorRole = UserRole.ADMINISTRATOR;
        final User originUser = User.of(administratorRole);

        final User sameRoleUser = User.of(administratorRole);

        Assertions.assertThat(originUser).isEqualTo(sameRoleUser);

    }

}
