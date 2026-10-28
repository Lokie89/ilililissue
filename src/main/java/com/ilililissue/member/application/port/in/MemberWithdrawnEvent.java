package com.ilililissue.member.application.port.in;

import com.ilililissue.member.domain.vo.MemberId;

/**
 * 회원이 탈퇴했을 때 발행한다. 다른 모듈은 이 이벤트로 회원별 데이터(로그인 토큰 등)를 정리한다.
 */
public record MemberWithdrawnEvent(MemberId memberId) {
}
