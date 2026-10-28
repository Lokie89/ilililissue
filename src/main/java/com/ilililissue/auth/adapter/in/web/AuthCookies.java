package com.ilililissue.auth.adapter.in.web;

import com.ilililissue.auth.application.port.in.AuthTokens;
import com.ilililissue.auth.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 인증 쿠키. 모두 HttpOnly, SameSite=Lax.
 * <ul>
 * <li>{@value #ACCESS_TOKEN}: 모든 API 경로로 전송, 수명 = 액세스 토큰 수명</li>
 * <li>{@value #REFRESH_TOKEN}: {@value #REFRESH_PATH} 경로로만 전송</li>
 * </ul>
 */
@RequiredArgsConstructor
@Component
public class AuthCookies {

    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String REFRESH_PATH = "/api/auth";

    private final AuthProperties authProperties;

    public HttpHeaders issue(AuthTokens tokens) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie(ACCESS_TOKEN, tokens.accessToken(), "/", tokens.accessTokenTtl()));
        headers.add(HttpHeaders.SET_COOKIE,
                cookie(REFRESH_TOKEN, tokens.refreshToken(), REFRESH_PATH, tokens.refreshTokenTtl()));
        return headers;
    }

    public HttpHeaders expire() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie(ACCESS_TOKEN, "", "/", Duration.ZERO));
        headers.add(HttpHeaders.SET_COOKIE, cookie(REFRESH_TOKEN, "", REFRESH_PATH, Duration.ZERO));
        return headers;
    }

    private String cookie(String name, String value, String path, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(authProperties.cookieSecure())
                .sameSite("Lax")
                .path(path)
                .maxAge(maxAge)
                .build()
                .toString();
    }
}
