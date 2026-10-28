package com.ilililissue.member.application.port.out;

import com.ilililissue.member.domain.entity.Member;

public interface SaveMemberPort {

    Member save(Member member);
}
