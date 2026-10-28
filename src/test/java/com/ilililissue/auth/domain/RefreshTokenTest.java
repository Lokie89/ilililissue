package com.ilililissue.auth.domain;

import com.ilililissue.member.domain.vo.MemberId;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class RefreshTokenTest {

    @DisplayName("원문 토큰은 매번 다르고 해시는 같은 원문에 대해 같다")
    @Test
    void generateAndHash() {
        String raw = RefreshToken.generateRawToken();

        Assertions.assertThat(RefreshToken.generateRawToken()).isNotEqualTo(raw);
        Assertions.assertThat(RefreshToken.hash(raw)).isEqualTo(RefreshToken.hash(raw)).isNotEqualTo(raw);
    }

    @DisplayName("만료 시각이 지나면 만료된 토큰이다")
    @Test
    void expired() {
        Instant expiresAt = Instant.parse("2026-10-28T00:00:00Z");
        RefreshToken token = RefreshToken.of(new MemberId(1), RefreshToken.hash("raw"), expiresAt);

        Assertions.assertThat(token.isExpired(expiresAt.minusSeconds(1))).isFalse();
        Assertions.assertThat(token.isExpired(expiresAt)).isTrue();
    }
}
