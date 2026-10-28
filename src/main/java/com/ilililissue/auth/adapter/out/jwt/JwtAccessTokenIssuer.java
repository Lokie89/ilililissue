package com.ilililissue.auth.adapter.out.jwt;

import com.ilililissue.auth.application.port.out.AccessTokenIssuer;
import com.ilililissue.member.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * 액세스 토큰: HS256, sub = 회원 id, role = 회원 역할.
 */
@RequiredArgsConstructor
@Component
public class JwtAccessTokenIssuer implements AccessTokenIssuer {

    public static final String ISSUER = "ilililissue";
    public static final String ROLE_CLAIM = "role";

    private final JwtEncoder jwtEncoder;

    @Override
    public String issue(Member member, Instant issuedAt, Duration ttl) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(member.getId().getValue()))
                .claim(ROLE_CLAIM, member.getRole().name())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(ttl))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
