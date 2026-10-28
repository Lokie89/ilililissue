package com.ilililissue.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * @param jwtSecret       HS256 서명 키. 32바이트 이상. 환경변수 {@code JWT_SECRET}으로 주입한다.
 * @param cookieSecure    인증 쿠키 Secure 속성. 운영은 true.
 * @param allowedOrigins  쿠키 인증을 허용할 클라이언트 Origin 목록 (CORS).
 */
@ConfigurationProperties("auth")
public record AuthProperties(
        String jwtSecret,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        boolean cookieSecure,
        List<String> allowedOrigins
) {

    public AuthProperties {
        if (jwtSecret == null || jwtSecret.getBytes().length < 32) {
            throw new IllegalStateException("auth.jwt-secret은 32바이트 이상이어야 합니다.");
        }
        accessTokenTtl = accessTokenTtl == null ? Duration.ofMinutes(30) : accessTokenTtl;
        refreshTokenTtl = refreshTokenTtl == null ? Duration.ofDays(14) : refreshTokenTtl;
        allowedOrigins = allowedOrigins == null ? List.of() : allowedOrigins;
    }
}
