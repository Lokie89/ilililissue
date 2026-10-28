package com.ilililissue.member.adapter.in.web;

import com.ilililissue.auth.adapter.in.web.AuthCookies;
import com.ilililissue.common.adapter.in.web.LoginMember;
import com.ilililissue.member.application.port.in.BlockMemberUseCase;
import com.ilililissue.member.application.port.in.BlockedMember;
import com.ilililissue.member.application.port.in.GetMemberQuery;
import com.ilililissue.member.application.port.in.RegisterMemberCommand;
import com.ilililissue.member.application.port.in.RegisterMemberUseCase;
import com.ilililissue.member.application.port.in.WithdrawMemberUseCase;
import com.ilililissue.member.domain.vo.MemberBlockId;
import com.ilililissue.member.domain.vo.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/members")
@RestController
public class MemberController {

    private final RegisterMemberUseCase registerMemberUseCase;
    private final GetMemberQuery getMemberQuery;
    private final WithdrawMemberUseCase withdrawMemberUseCase;
    private final BlockMemberUseCase blockMemberUseCase;
    private final AuthCookies authCookies;

    @PostMapping
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        MemberId id = registerMemberUseCase.register(
                new RegisterMemberCommand(request.email(), request.nickname(), request.password(),
                        Boolean.TRUE.equals(request.agreedToTerms())));
        return ResponseEntity.created(URI.create("/api/members/" + id.getValue()))
                .body(new RegisterResponse(id.getValue()));
    }

    @GetMapping("/me")
    public MemberResponse me(@LoginMember MemberId memberId) {
        return MemberResponse.from(getMemberQuery.getMember(memberId));
    }

    /**
     * 비밀번호를 확인하고 탈퇴한다. 인증 쿠키를 지우고, 모든 기기의 리프레시 토큰을 폐기한다.
     */
    @PostMapping("/me/withdrawal")
    public ResponseEntity<Void> withdraw(@LoginMember MemberId memberId, @RequestBody WithdrawRequest request) {
        withdrawMemberUseCase.withdraw(memberId, request.password());
        return ResponseEntity.noContent().headers(authCookies.expire()).build();
    }

    /**
     * 내가 차단한 회원 목록. 차단은 의견 목록에서 한다({@code POST /api/opinions/{id}/author-block}).
     */
    @GetMapping("/me/blocks")
    public List<BlockResponse> blocks(@LoginMember MemberId memberId) {
        return blockMemberUseCase.getBlocks(memberId).stream().map(BlockResponse::from).toList();
    }

    @DeleteMapping("/me/blocks/{blockId}")
    public ResponseEntity<Void> unblock(@LoginMember MemberId memberId, @PathVariable long blockId) {
        blockMemberUseCase.unblock(memberId, new MemberBlockId(blockId));
        return ResponseEntity.noContent().build();
    }

    public record BlockResponse(long id, String nickname, LocalDateTime blockedAt) {

        static BlockResponse from(BlockedMember blocked) {
            return new BlockResponse(blocked.blockId().getValue(), blocked.nickname(), blocked.blockedAt());
        }
    }

    public record WithdrawRequest(String password) {
    }

    /**
     * @param agreedToTerms 이용약관·개인정보 처리방침 동의. true가 아니면 400.
     */
    public record RegisterRequest(String email, String nickname, String password, Boolean agreedToTerms) {
    }

    public record RegisterResponse(long id) {
    }
}
