package com.ilililissue.member.adapter.out.persistence;

import com.ilililissue.member.application.port.out.MemberBlockPort;
import com.ilililissue.member.domain.entity.MemberBlock;
import com.ilililissue.member.domain.vo.MemberBlockId;
import com.ilililissue.member.domain.vo.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class MemberBlockPersistenceAdapter implements MemberBlockPort {

    private final MemberBlockJpaRepository memberBlockJpaRepository;

    @Override
    public void save(MemberBlock block) {
        memberBlockJpaRepository.save(new MemberBlockJpaEntity(
                null,
                block.getBlockerId().getValue(),
                block.getBlockedId().getValue(),
                block.getBlockedAt()
        ));
    }

    @Override
    public boolean exists(MemberId blockerId, MemberId blockedId) {
        return memberBlockJpaRepository.existsByBlockerIdAndBlockedId(blockerId.getValue(), blockedId.getValue());
    }

    @Override
    public void delete(MemberId blockerId, MemberBlockId blockId) {
        memberBlockJpaRepository.deleteByIdAndBlockerId(blockId.getValue(), blockerId.getValue());
    }

    @Override
    public List<MemberBlock> findByBlockerId(MemberId blockerId) {
        return memberBlockJpaRepository.findByBlockerIdOrderByIdDesc(blockerId.getValue()).stream()
                .map(entity -> MemberBlock.withId(
                        new MemberBlockId(entity.getId()),
                        new MemberId(entity.getBlockerId()),
                        new MemberId(entity.getBlockedId()),
                        entity.getBlockedAt()))
                .toList();
    }

    @Override
    public Set<MemberId> findBlockedIds(MemberId blockerId) {
        return memberBlockJpaRepository.findBlockedIds(blockerId.getValue()).stream()
                .map(MemberId::new)
                .collect(Collectors.toSet());
    }
}
