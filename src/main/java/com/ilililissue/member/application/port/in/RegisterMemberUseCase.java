package com.ilililissue.member.application.port.in;

import com.ilililissue.member.domain.vo.MemberId;

public interface RegisterMemberUseCase {

    MemberId register(RegisterMemberCommand command);
}
