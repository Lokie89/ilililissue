package com.ilililissue.member.application.port.out;

import com.ilililissue.member.domain.entity.MemberBlock;
import com.ilililissue.member.domain.vo.MemberBlockId;
import com.ilililissue.member.domain.vo.MemberId;

import java.util.List;
import java.util.Set;

public interface MemberBlockPort {

    void save(MemberBlock block);

    boolean exists(MemberId blockerId, MemberId blockedId);

    void delete(MemberId blockerId, MemberBlockId blockId);

    /**
     * 최근 차단 순
     */
    List<MemberBlock> findByBlockerId(MemberId blockerId);

    Set<MemberId> findBlockedIds(MemberId blockerId);
}
