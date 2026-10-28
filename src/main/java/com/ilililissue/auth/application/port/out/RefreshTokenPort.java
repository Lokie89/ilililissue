package com.ilililissue.auth.application.port.out;

import com.ilililissue.auth.domain.RefreshToken;

import java.util.Optional;

public interface RefreshTokenPort {

    void save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    boolean deleteByTokenHash(String tokenHash);
}
