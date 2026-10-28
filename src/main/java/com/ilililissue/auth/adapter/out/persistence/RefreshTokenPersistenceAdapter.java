package com.ilililissue.auth.adapter.out.persistence;

import com.ilililissue.auth.application.port.out.RefreshTokenPort;
import com.ilililissue.auth.domain.RefreshToken;
import com.ilililissue.member.domain.vo.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class RefreshTokenPersistenceAdapter implements RefreshTokenPort {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Override
    public void save(RefreshToken refreshToken) {
        refreshTokenJpaRepository.save(new RefreshTokenJpaEntity(
                null,
                refreshToken.getMemberId().getValue(),
                refreshToken.getTokenHash(),
                refreshToken.getExpiresAt()
        ));
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenJpaRepository.findByTokenHash(tokenHash)
                .map(entity -> RefreshToken.of(
                        new MemberId(entity.getMemberId()),
                        entity.getTokenHash(),
                        entity.getExpiresAt()
                ));
    }

    @Override
    public boolean deleteByTokenHash(String tokenHash) {
        return refreshTokenJpaRepository.deleteByTokenHash(tokenHash) > 0;
    }
}
