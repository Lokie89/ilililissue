package com.ilililissue.member.domain.entity;

import com.ilililissue.common.SelfValidating;
import com.ilililissue.member.domain.SelfBlockException;
import com.ilililissue.member.domain.vo.MemberBlockId;
import com.ilililissue.member.domain.vo.MemberId;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * <h2>회원 차단 도메인</h2>
 * <ul>
 * <li>차단한 회원(blocker)의 의견 목록에서 차단된 회원(blocked)의 의견을 뺀다. 한쪽 방향이며 상대에게 알리지 않는다.</li>
 * <li>(blocker, blocked) 쌍은 하나뿐이며 저장소의 유니크 제약으로 보장한다.</li>
 * <li>자기 자신은 차단할 수 없다.</li>
 * <li>차단은 언제든 해제할 수 있다.</li>
 * </ul>
 */
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
public class MemberBlock extends SelfValidating<MemberBlock> {

    private final MemberBlockId id;

    @NotNull
    private final MemberId blockerId;

    @NotNull
    private final MemberId blockedId;

    @NotNull
    private final LocalDateTime blockedAt;

    private MemberBlock(
            MemberBlockId id,
            MemberId blockerId,
            MemberId blockedId,
            LocalDateTime blockedAt
    ) {
        this.id = id;
        this.blockerId = blockerId;
        this.blockedId = blockedId;
        this.blockedAt = blockedAt;
        this.validate();
    }

    public static MemberBlock of(MemberId blockerId, MemberId blockedId, LocalDateTime now) {
        if (blockerId != null && blockerId.equals(blockedId)) {
            throw new SelfBlockException();
        }
        return new MemberBlock(null, blockerId, blockedId, now);
    }

    public static MemberBlock withId(MemberBlockId id, MemberId blockerId, MemberId blockedId,
                                     LocalDateTime blockedAt) {
        return new MemberBlock(id, blockerId, blockedId, blockedAt);
    }
}
