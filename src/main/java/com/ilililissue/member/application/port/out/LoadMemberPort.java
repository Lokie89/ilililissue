package com.ilililissue.member.application.port.out;

import com.ilililissue.member.domain.entity.Member;
import com.ilililissue.member.domain.vo.Email;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.member.domain.vo.Nickname;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface LoadMemberPort {

    Optional<Member> findById(MemberId id);

    Optional<Member> findByEmail(Email email);

    boolean existsByEmail(Email email);

    boolean existsByNickname(Nickname nickname);

    /**
     * 탈퇴한 회원은 결과에 포함하지 않는다.
     */
    Map<MemberId, Nickname> findNicknames(Collection<MemberId> ids);

    /**
     * 가입되어 있고 탈퇴하지 않은 회원이면 true
     */
    boolean isActive(MemberId id);
}
