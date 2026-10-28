package com.ilililissue.auth.domain;

import com.ilililissue.common.SelfValidating;
import com.ilililissue.member.domain.vo.MemberId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * <h2>리프레시 토큰</h2>
 * <ul>
 * <li>원문 토큰은 클라이언트 쿠키에만 있고 서버는 SHA-256 해시만 보관한다.</li>
 * <li>재발급 시 기존 토큰을 폐기하고 새 토큰을 발급한다(rotation).</li>
 * <li>로그아웃 시 폐기한다.</li>
 * </ul>
 */
@Getter
public class RefreshToken extends SelfValidating<RefreshToken> {

    private static final SecureRandom RANDOM = new SecureRandom();

    @NotNull
    private final MemberId memberId;

    @NotBlank
    private final String tokenHash;

    @NotNull
    private final Instant expiresAt;

    private RefreshToken(
            MemberId memberId,
            String tokenHash,
            Instant expiresAt
    ) {
        this.memberId = memberId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.validate();
    }

    public static RefreshToken of(MemberId memberId, String tokenHash, Instant expiresAt) {
        return new RefreshToken(memberId, tokenHash, expiresAt);
    }

    public static String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(this.expiresAt);
    }
}
