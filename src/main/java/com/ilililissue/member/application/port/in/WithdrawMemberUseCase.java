package com.ilililissue.member.application.port.in;

import com.ilililissue.member.domain.vo.MemberId;

public interface WithdrawMemberUseCase {

    /**
     * 비밀번호를 확인한 뒤 탈퇴 처리하고 {@link MemberWithdrawnEvent}를 발행한다.
     */
    void withdraw(MemberId memberId, String rawPassword);
}
