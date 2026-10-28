package com.ilililissue.vote.adapter.in.web;

import com.ilililissue.common.adapter.in.web.LoginMember;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.vote.application.port.in.VoteResult;
import com.ilililissue.vote.application.port.in.VoteUseCase;
import com.ilililissue.vote.domain.entity.Vote;
import com.ilililissue.vote.domain.vo.Stance;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/issues/{issueId}")
@RestController
public class VoteController {

    static final String RESULT_NOTICE = "서비스 참여자 기준 결과이며 대표성 있는 여론조사가 아닙니다.";

    private final VoteUseCase voteUseCase;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/votes")
    public VoteResponse vote(@PathVariable long issueId, @LoginMember MemberId memberId,
                             @RequestBody VoteRequest request) {
        return VoteResponse.from(voteUseCase.cast(new IssueId(issueId), memberId, request.stance()));
    }

    @GetMapping("/results")
    public ResultResponse results(@PathVariable long issueId, @LoginMember MemberId memberId) {
        return ResultResponse.from(voteUseCase.getResult(new IssueId(issueId), memberId));
    }

    public record VoteRequest(Stance stance) {
    }

    public record VoteResponse(long issueId, Stance stance, LocalDateTime votedAt) {

        static VoteResponse from(Vote vote) {
            return new VoteResponse(vote.getIssueId().getValue(), vote.getStance(), vote.getVotedAt());
        }
    }

    public record ResultResponse(long total, List<StanceResult> stances, String notice) {

        static ResultResponse from(VoteResult result) {
            return new ResultResponse(
                    result.total(),
                    Arrays.stream(Stance.values())
                            .map(stance -> new StanceResult(stance, result.counts().get(stance),
                                    result.percentage(stance)))
                            .toList(),
                    RESULT_NOTICE
            );
        }
    }

    public record StanceResult(Stance stance, long count, double percentage) {
    }
}
