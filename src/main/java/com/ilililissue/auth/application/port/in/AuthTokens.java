package com.ilililissue.auth.application.port.in;

import com.ilililissue.member.domain.entity.Member;

import java.time.Duration;

public record AuthTokens(
        Member member,
        String accessToken,
        Duration accessTokenTtl,
        String refreshToken,
        Duration refreshTokenTtl
) {
}
