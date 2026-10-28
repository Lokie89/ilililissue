package com.ilililissue.member.application.port.out;

import com.ilililissue.member.domain.entity.Member;
import com.ilililissue.member.domain.vo.Email;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.member.domain.vo.Nickname;

import java.util.Optional;

public interface LoadMemberPort {

    Optional<Member> findById(MemberId id);

    Optional<Member> findByEmail(Email email);

    boolean existsByEmail(Email email);

    boolean existsByNickname(Nickname nickname);
}
