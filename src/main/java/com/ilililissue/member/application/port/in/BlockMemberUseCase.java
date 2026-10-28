package com.ilililissue.member.application.port.in;

import com.ilililissue.member.domain.vo.MemberBlockId;
import com.ilililissue.member.domain.vo.MemberId;

import java.util.List;
import java.util.Set;

public interface BlockMemberUseCase {

    /**
     * 회원을 차단한다. 이미 차단했으면 아무 일도 하지 않는다.
     */
    void block(MemberId blockerId, MemberId blockedId);

    /**
     * 내 차단을 해제한다. 없거나 남의 차단이면 아무 일도 하지 않는다.
     */
    void unblock(MemberId blockerId, MemberBlockId blockId);

    /**
     * 내가 차단한 회원 목록(최근 차단 순). 탈퇴한 회원의 닉네임은 {@code Member.WITHDRAWN_NICKNAME}이다.
     */
    List<BlockedMember> getBlocks(MemberId blockerId);

    /**
     * 내가 차단한 회원 ID. 의견 목록에서 제외할 때 쓴다.
     */
    Set<MemberId> findBlockedIds(MemberId blockerId);
}
