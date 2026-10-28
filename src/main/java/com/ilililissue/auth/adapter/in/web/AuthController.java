package com.ilililissue.auth.adapter.in.web;

import com.ilililissue.auth.application.port.in.AuthTokens;
import com.ilililissue.auth.application.port.in.AuthUseCase;
import com.ilililissue.member.adapter.in.web.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthUseCase authUseCase;
    private final AuthCookies authCookies;

    @PostMapping("/login")
    public ResponseEntity<MemberResponse> login(@RequestBody LoginRequest request) {
        AuthTokens tokens = authUseCase.login(request.email(), request.password());
        return ResponseEntity.ok()
                .headers(authCookies.issue(tokens))
                .body(MemberResponse.from(tokens.member()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<MemberResponse> refresh(
            @CookieValue(name = AuthCookies.REFRESH_TOKEN, required = false) String refreshToken) {
        AuthTokens tokens = authUseCase.refresh(refreshToken);
        return ResponseEntity.ok()
                .headers(authCookies.issue(tokens))
                .body(MemberResponse.from(tokens.member()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = AuthCookies.REFRESH_TOKEN, required = false) String refreshToken) {
        authUseCase.logout(refreshToken);
        return ResponseEntity.noContent().headers(authCookies.expire()).build();
    }

    public record LoginRequest(String email, String password) {
    }
}
