package com.ilililissue.auth.adapter.in.web;

import com.ilililissue.auth.application.port.in.AuthTokens;
import com.ilililissue.auth.application.port.in.AuthUseCase;
import com.ilililissue.member.adapter.in.web.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인·재발급·로그아웃.
 * <ul>
 * <li>웹(기본): 토큰을 HttpOnly 쿠키로 주고 받는다. 응답 본문은 회원 정보뿐이다.</li>
 * <li>앱({@value #CLIENT_HEADER}: {@value #APP_CLIENT}): 토큰을 응답 본문으로 주고, 쿠키는 쓰지 않는다.
 * 재발급·로그아웃은 본문의 {@code refreshToken}을 받는다. 앱은 액세스 토큰을 {@code Authorization: Bearer}로 보낸다.</li>
 * </ul>
 */
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    public static final String CLIENT_HEADER = "X-Client";
    public static final String APP_CLIENT = "app";

    private final AuthUseCase authUseCase;
    private final AuthCookies authCookies;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   @RequestHeader(name = CLIENT_HEADER, required = false) String client) {
        return respond(authUseCase.login(request.email(), request.password()), client);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = AuthCookies.REFRESH_TOKEN, required = false) String cookieToken,
            @RequestHeader(name = CLIENT_HEADER, required = false) String client,
            @RequestBody(required = false) RefreshTokenRequest body) {
        return respond(authUseCase.refresh(refreshToken(client, cookieToken, body)), client);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = AuthCookies.REFRESH_TOKEN, required = false) String cookieToken,
            @RequestHeader(name = CLIENT_HEADER, required = false) String client,
            @RequestBody(required = false) RefreshTokenRequest body) {
        authUseCase.logout(refreshToken(client, cookieToken, body));
        if (isApp(client)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.noContent().headers(authCookies.expire()).build();
    }

    private ResponseEntity<?> respond(AuthTokens tokens, String client) {
        if (isApp(client)) {
            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(AppTokenResponse.from(tokens));
        }
        return ResponseEntity.ok()
                .headers(authCookies.issue(tokens))
                .body(MemberResponse.from(tokens.member()));
    }

    private static String refreshToken(String client, String cookieToken, RefreshTokenRequest body) {
        if (isApp(client)) {
            return body == null ? null : body.refreshToken();
        }
        return cookieToken;
    }

    private static boolean isApp(String client) {
        return APP_CLIENT.equalsIgnoreCase(client);
    }

    public record LoginRequest(String email, String password) {
    }

    public record RefreshTokenRequest(String refreshToken) {
    }

    /**
     * 앱용 토큰 응답. 만료 시간은 초 단위다.
     */
    public record AppTokenResponse(MemberResponse member, String accessToken, long accessTokenExpiresIn,
                                   String refreshToken, long refreshTokenExpiresIn) {

        static AppTokenResponse from(AuthTokens tokens) {
            return new AppTokenResponse(
                    MemberResponse.from(tokens.member()),
                    tokens.accessToken(),
                    tokens.accessTokenTtl().toSeconds(),
                    tokens.refreshToken(),
                    tokens.refreshTokenTtl().toSeconds()
            );
        }
    }
}
