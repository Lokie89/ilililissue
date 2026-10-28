package com.ilililissue.member.adapter.in.web;

import com.ilililissue.common.adapter.in.web.LoginMember;
import com.ilililissue.member.application.port.in.GetMemberQuery;
import com.ilililissue.member.application.port.in.RegisterMemberCommand;
import com.ilililissue.member.application.port.in.RegisterMemberUseCase;
import com.ilililissue.member.domain.vo.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RequiredArgsConstructor
@RequestMapping("/api/members")
@RestController
public class MemberController {

    private final RegisterMemberUseCase registerMemberUseCase;
    private final GetMemberQuery getMemberQuery;

    @PostMapping
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        MemberId id = registerMemberUseCase.register(
                new RegisterMemberCommand(request.email(), request.nickname(), request.password()));
        return ResponseEntity.created(URI.create("/api/members/" + id.getValue()))
                .body(new RegisterResponse(id.getValue()));
    }

    @GetMapping("/me")
    public MemberResponse me(@LoginMember MemberId memberId) {
        return MemberResponse.from(getMemberQuery.getMember(memberId));
    }

    public record RegisterRequest(String email, String nickname, String password) {
    }

    public record RegisterResponse(long id) {
    }
}
