package com.ilililissue.auth.application.service;

import com.ilililissue.auth.application.port.out.RefreshTokenPort;
import com.ilililissue.member.application.port.in.MemberWithdrawnEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 탈퇴한 회원의 리프레시 토큰을 모두 폐기한다. 탈퇴 트랜잭션 안에서 함께 처리된다.
 */
@RequiredArgsConstructor
@Component
public class MemberWithdrawnListener {

    private final RefreshTokenPort refreshTokenPort;

    @EventListener
    public void revokeRefreshTokens(MemberWithdrawnEvent event) {
        refreshTokenPort.deleteByMemberId(event.memberId());
    }
}
