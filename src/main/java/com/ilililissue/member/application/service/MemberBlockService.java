package com.ilililissue.member.application.service;

import com.ilililissue.member.application.port.in.BlockMemberUseCase;
import com.ilililissue.member.application.port.in.BlockedMember;
import com.ilililissue.member.application.port.out.LoadMemberPort;
import com.ilililissue.member.application.port.out.MemberBlockPort;
import com.ilililissue.member.domain.entity.Member;
import com.ilililissue.member.domain.entity.MemberBlock;
import com.ilililissue.member.domain.vo.MemberBlockId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.member.domain.vo.Nickname;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class MemberBlockService implements BlockMemberUseCase {

    private final MemberBlockPort memberBlockPort;
    private final LoadMemberPort loadMemberPort;
    private final Clock clock;

    @Transactional
    @Override
    public void block(MemberId blockerId, MemberId blockedId) {
        MemberBlock block = MemberBlock.of(blockerId, blockedId, LocalDateTime.now(clock));
        if (memberBlockPort.exists(blockerId, blockedId)) {
            return;
        }
        memberBlockPort.save(block);
    }

    @Transactional
    @Override
    public void unblock(MemberId blockerId, MemberBlockId blockId) {
        memberBlockPort.delete(blockerId, blockId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BlockedMember> getBlocks(MemberId blockerId) {
        List<MemberBlock> blocks = memberBlockPort.findByBlockerId(blockerId);
        Map<MemberId, Nickname> nicknames = loadMemberPort.findNicknames(
                blocks.stream().map(MemberBlock::getBlockedId).toList());
        return blocks.stream()
                .map(block -> {
                    Nickname nickname = nicknames.get(block.getBlockedId());
                    return new BlockedMember(block.getId(),
                            nickname == null ? Member.WITHDRAWN_NICKNAME : nickname.getValue(),
                            block.getBlockedAt());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Set<MemberId> findBlockedIds(MemberId blockerId) {
        return memberBlockPort.findBlockedIds(blockerId);
    }
}
