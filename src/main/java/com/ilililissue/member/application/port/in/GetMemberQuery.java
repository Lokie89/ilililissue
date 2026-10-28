package com.ilililissue.member.application.port.in;

import com.ilililissue.member.domain.entity.Member;
import com.ilililissue.member.domain.vo.MemberId;

public interface GetMemberQuery {

    Member getMember(MemberId id);
}
