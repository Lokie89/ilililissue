package com.ilililissue.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * <p>회원 정책 테스트</p>
 * <li>관리자 여부</li>
 */
public class MemberTest {

    @DisplayName("관리자 여부 확인")
    @Test
    void isAdministrator() {
        Member member = Member.builder(MemberRole.ADMINISTRATOR)
                .build();

        Assertions.assertThat(member.isAdministrator()).isTrue();
    }
}
