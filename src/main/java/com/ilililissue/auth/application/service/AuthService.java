package com.ilililissue.auth.application.service;

import com.ilililissue.auth.application.AuthenticationFailedException;
import com.ilililissue.auth.application.port.in.AuthTokens;
import com.ilililissue.auth.application.port.in.AuthUseCase;
import com.ilililissue.auth.application.port.out.AccessTokenIssuer;
import com.ilililissue.auth.application.port.out.RefreshTokenPort;
import com.ilililissue.auth.config.AuthProperties;
import com.ilililissue.auth.domain.RefreshToken;
import com.ilililissue.member.application.port.out.LoadMemberPort;
import com.ilililissue.member.domain.entity.Member;
import com.ilililissue.member.domain.vo.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService implements AuthUseCase {

    private final LoadMemberPort loadMemberPort;
    private final RefreshTokenPort refreshTokenPort;
    private final AccessTokenIssuer accessTokenIssuer;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;
    private final Clock clock;

    private volatile String dummyEncodedPassword;

    @Transactional
    @Override
    public AuthTokens login(String email, String rawPassword) {
        Optional<Member> member = findByEmail(email);
        // 존재하지 않는 이메일도 같은 비용으로 비교해 응답 시간으로 가입 여부가 드러나지 않게 한다.
        String encodedPassword = member.map(Member::getEncodedPassword).orElseGet(this::dummyEncodedPassword);
        boolean matches = rawPassword != null && passwordEncoder.matches(rawPassword, encodedPassword);
        if (member.isEmpty() || !matches) {
            throw AuthenticationFailedException.invalidCredentials();
        }
        return issueTokens(member.get());
    }

    @Transactional
    @Override
    public AuthTokens refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw AuthenticationFailedException.invalidRefreshToken();
        }
        String tokenHash = RefreshToken.hash(rawRefreshToken);
        RefreshToken stored = refreshTokenPort.findByTokenHash(tokenHash)
                .orElseThrow(AuthenticationFailedException::invalidRefreshToken);
        // 삭제에 성공한 요청만 재발급한다. 같은 토큰으로 동시에 들어온 재발급 요청은 하나만 성공한다.
        boolean consumed = refreshTokenPort.deleteByTokenHash(tokenHash);
        if (!consumed || stored.isExpired(clock.instant())) {
            throw AuthenticationFailedException.invalidRefreshToken();
        }
        Member member = loadMemberPort.findById(stored.getMemberId())
                .orElseThrow(AuthenticationFailedException::invalidRefreshToken);
        return issueTokens(member);
    }

    @Transactional
    @Override
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokenPort.deleteByTokenHash(RefreshToken.hash(rawRefreshToken));
    }

    private String dummyEncodedPassword() {
        if (this.dummyEncodedPassword == null) {
            this.dummyEncodedPassword = passwordEncoder.encode(RefreshToken.generateRawToken());
        }
        return this.dummyEncodedPassword;
    }

    private Optional<Member> findByEmail(String email) {
        try {
            return loadMemberPort.findByEmail(Email.from(email));
        } catch (RuntimeException invalidEmail) {
            return Optional.empty();
        }
    }

    private AuthTokens issueTokens(Member member) {
        Instant now = clock.instant();
        String accessToken = accessTokenIssuer.issue(member, now, authProperties.accessTokenTtl());
        String rawRefreshToken = RefreshToken.generateRawToken();
        refreshTokenPort.save(RefreshToken.of(
                member.getId(),
                RefreshToken.hash(rawRefreshToken),
                now.plus(authProperties.refreshTokenTtl())
        ));
        return new AuthTokens(member, accessToken, authProperties.accessTokenTtl(),
                rawRefreshToken, authProperties.refreshTokenTtl());
    }
}
