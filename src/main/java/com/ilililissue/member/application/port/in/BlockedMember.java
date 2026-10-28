package com.ilililissue.member.application.port.in;

import com.ilililissue.member.domain.vo.MemberBlockId;

import java.time.LocalDateTime;

/**
 * 차단 목록의 한 항목. 회원 ID는 노출하지 않고 차단 ID로 해제한다.
 */
public record BlockedMember(MemberBlockId blockId, String nickname, LocalDateTime blockedAt) {
}
