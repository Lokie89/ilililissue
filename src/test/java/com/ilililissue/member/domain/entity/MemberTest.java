package com.ilililissue.member.domain.entity;

import com.ilililissue.member.domain.vo.Email;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.member.domain.vo.MemberRole;
import com.ilililissue.member.domain.vo.Nickname;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MemberTest {

    @DisplayName("가입한 회원은 USER 역할을 가진다")
    @Test
    void register() {
        Member member = Member.register(Email.from("user@example.com"), Nickname.from("토론러"), "{bcrypt}hash");

        Assertions.assertThat(member.getRole()).isEqualTo(MemberRole.USER);
        Assertions.assertThat(member.isAdministrator()).isFalse();
    }

    @DisplayName("잘못된 이메일, 닉네임, 빈 비밀번호로 가입할 수 없다")
    @Test
    void invalidRegister() {
        Assertions.assertThatThrownBy(() -> Email.from("not-an-email"));
        Assertions.assertThatThrownBy(() -> Nickname.from("a"));
        Assertions.assertThatThrownBy(() -> Nickname.from("열세글자를넘는아주긴닉네임입니다"));
        Assertions.assertThatThrownBy(
                () -> Member.register(Email.from("user@example.com"), Nickname.from("토론러"), " "));
    }

    @DisplayName("회원 동일성은 id로 판단한다")
    @Test
    void equals() {
        Member origin = Member.withId(new MemberId(1), Email.from("a@example.com"), Nickname.from("에이"),
                "hash", MemberRole.USER);
        Member sameId = Member.withId(new MemberId(1), Email.from("b@example.com"), Nickname.from("비이"),
                "hash", MemberRole.ADMIN);
        Member differentId = Member.withId(new MemberId(2), Email.from("a@example.com"), Nickname.from("에이"),
                "hash", MemberRole.USER);

        Assertions.assertThat(origin).isEqualTo(sameId);
        Assertions.assertThat(origin).isNotEqualTo(differentId);
        Assertions.assertThat(sameId.isAdministrator()).isTrue();
    }
}
