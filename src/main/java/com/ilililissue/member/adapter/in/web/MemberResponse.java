package com.ilililissue.member.adapter.in.web;

import com.ilililissue.member.domain.entity.Member;

public record MemberResponse(long id, String email, String nickname, String role) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId().getValue(),
                member.getEmail().getValue(),
                member.getNickname().getValue(),
                member.getRole().name()
        );
    }
}
