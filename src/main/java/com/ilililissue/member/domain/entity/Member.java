package com.ilililissue.member.domain.entity;

import com.ilililissue.common.SelfValidating;
import com.ilililissue.member.domain.AlreadyWithdrawnMemberException;
import com.ilililissue.member.domain.vo.Email;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.member.domain.vo.MemberRole;
import com.ilililissue.member.domain.vo.Nickname;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * <h2>회원 도메인</h2>
 * <ul>
 * <li>가입한 회원은 USER 역할을 가진다.</li>
 * <li>비밀번호는 암호화된 값만 가진다.</li>
 * <li>이메일, 닉네임의 유일성은 저장소에서 보장한다.</li>
 * <li>탈퇴하면 이메일·닉네임·비밀번호를 지운다. 투표와 의견은 남고 작성자는 {@value #WITHDRAWN_NICKNAME}으로 보인다.
 * 지운 이메일·닉네임으로 다시 가입할 수 있다.</li>
 * <li>가입할 때 이용약관·개인정보 처리방침 동의 시각을 남긴다. 탈퇴해도 지우지 않는다(동의 기록).
 * 동의 절차가 생기기 전 가입한 회원은 null이다.</li>
 * </ul>
 */
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
public class Member extends SelfValidating<Member> {

    public static final String WITHDRAWN_NICKNAME = "탈퇴한 회원";

    private final MemberId id;

    private Email email;

    private Nickname nickname;

    private String encodedPassword;

    @NotNull
    private final MemberRole role;

    private LocalDateTime withdrawnAt;

    private final LocalDateTime termsAgreedAt;

    private Member(
            MemberId id,
            Email email,
            Nickname nickname,
            String encodedPassword,
            MemberRole role,
            LocalDateTime withdrawnAt,
            LocalDateTime termsAgreedAt
    ) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.encodedPassword = encodedPassword;
        this.role = role;
        this.withdrawnAt = withdrawnAt;
        this.termsAgreedAt = termsAgreedAt;
        this.validate();
    }

    public static Member register(Email email, Nickname nickname, String encodedPassword,
                                  LocalDateTime termsAgreedAt) {
        return new Member(null, email, nickname, encodedPassword, MemberRole.USER, null, termsAgreedAt);
    }

    public static Member withId(MemberId id, Email email, Nickname nickname, String encodedPassword, MemberRole role) {
        return new Member(id, email, nickname, encodedPassword, role, null, null);
    }

    public static Member withId(MemberId id, Email email, Nickname nickname, String encodedPassword, MemberRole role,
                                LocalDateTime withdrawnAt) {
        return new Member(id, email, nickname, encodedPassword, role, withdrawnAt, null);
    }

    public static Member withId(MemberId id, Email email, Nickname nickname, String encodedPassword, MemberRole role,
                                LocalDateTime withdrawnAt, LocalDateTime termsAgreedAt) {
        return new Member(id, email, nickname, encodedPassword, role, withdrawnAt, termsAgreedAt);
    }

    public boolean isAdministrator() {
        return this.role == MemberRole.ADMIN;
    }

    public boolean isWithdrawn() {
        return this.withdrawnAt != null;
    }

    public void withdraw(LocalDateTime now) {
        if (isWithdrawn()) {
            throw new AlreadyWithdrawnMemberException();
        }
        this.email = null;
        this.nickname = null;
        this.encodedPassword = null;
        this.withdrawnAt = now;
        this.validate();
    }

    @AssertTrue(message = "탈퇴하지 않은 회원은 이메일, 닉네임, 비밀번호가 있어야 합니다.")
    public boolean isActiveMemberComplete() {
        return isWithdrawn()
                || (email != null && nickname != null && encodedPassword != null && !encodedPassword.isBlank());
    }
}
