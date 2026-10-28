package com.ilililissue.member.adapter.out.persistence;

import com.ilililissue.member.domain.vo.MemberRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 탈퇴한 회원은 이메일·닉네임·비밀번호가 null이다. 유니크 제약은 null을 중복으로 보지 않는다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_member_nickname", columnNames = "nickname")})
@Entity
public class MemberJpaEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(length = 320)
    private String email;

    @Column(length = 12)
    private String nickname;

    private String encodedPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MemberRole role;

    private LocalDateTime withdrawnAt;

    private LocalDateTime termsAgreedAt;
}
