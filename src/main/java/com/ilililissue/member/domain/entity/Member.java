package com.ilililissue.member.domain.entity;

import com.ilililissue.common.SelfValidating;
import com.ilililissue.member.domain.vo.Email;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.member.domain.vo.MemberRole;
import com.ilililissue.member.domain.vo.Nickname;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * <h2>회원 도메인</h2>
 * <ul>
 * <li>가입한 회원은 USER 역할을 가진다.</li>
 * <li>비밀번호는 암호화된 값만 가진다.</li>
 * <li>이메일, 닉네임의 유일성은 저장소에서 보장한다.</li>
 * </ul>
 */
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
public class Member extends SelfValidating<Member> {

    private final MemberId id;

    @NotNull
    private final Email email;

    @NotNull
    private final Nickname nickname;

    @NotBlank
    private final String encodedPassword;

    @NotNull
    private final MemberRole role;

    private Member(
            MemberId id,
            Email email,
            Nickname nickname,
            String encodedPassword,
            MemberRole role
    ) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.encodedPassword = encodedPassword;
        this.role = role;
        this.validate();
    }

    public static Member register(Email email, Nickname nickname, String encodedPassword) {
        return new Member(null, email, nickname, encodedPassword, MemberRole.USER);
    }

    public static Member withId(MemberId id, Email email, Nickname nickname, String encodedPassword, MemberRole role) {
        return new Member(id, email, nickname, encodedPassword, role);
    }

    public boolean isAdministrator() {
        return this.role == MemberRole.ADMIN;
    }
}
