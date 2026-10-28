package com.ilililissue.member.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberBlockJpaRepository extends JpaRepository<MemberBlockJpaEntity, Long> {

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    List<MemberBlockJpaEntity> findByBlockerIdOrderByIdDesc(Long blockerId);

    @Query("select b.blockedId from MemberBlockJpaEntity b where b.blockerId = :blockerId")
    List<Long> findBlockedIds(Long blockerId);

    @Modifying
    @Query("delete from MemberBlockJpaEntity b where b.id = :id and b.blockerId = :blockerId")
    int deleteByIdAndBlockerId(Long id, Long blockerId);
}
