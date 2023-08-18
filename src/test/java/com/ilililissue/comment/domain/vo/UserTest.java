package com.ilililissue.comment.domain.vo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
public class UserTest {

    @DisplayName("댓글 생성자 동치성 테스트")
    @Test
    void equals() {

        final Id originId = new Id(5);
        final String originUsername = "ENTJ";
        User originUser = User.of(originId, originUsername);

        User sameIdUser = User.of(originId, originUsername);
        Assertions.assertThat(originUser).isEqualTo(sameIdUser);

        final Id sameValueId = new Id(5);

        User sameValueIdUser = User.of(sameValueId, originUsername);
        Assertions.assertThat(originUser).isEqualTo(sameValueIdUser);

        final String differentUsername = "INFP";

        User sameValueIdDifferentUsernameUser = User.of(sameValueId, differentUsername);
        Assertions.assertThat(originUser).isEqualTo(sameValueIdDifferentUsernameUser);

        final Id differentValueId = new Id(6);

        User differentValueIdSameUsernameUser = User.of(differentValueId, originUsername);
        Assertions.assertThat(originUser).isNotEqualTo(differentValueIdSameUsernameUser);

    }
}
