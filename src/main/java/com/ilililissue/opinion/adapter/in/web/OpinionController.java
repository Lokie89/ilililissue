package com.ilililissue.opinion.adapter.in.web;

import com.ilililissue.common.adapter.in.web.LoginMember;
import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.issue.application.port.in.IssueQuery;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.opinion.application.port.in.OpinionItem;
import com.ilililissue.opinion.application.port.in.OpinionSort;
import com.ilililissue.opinion.application.port.in.OpinionUseCase;
import com.ilililissue.opinion.domain.entity.Opinion;
import com.ilililissue.opinion.domain.vo.OpinionContent;
import com.ilililissue.opinion.domain.vo.OpinionId;
import com.ilililissue.vote.application.port.in.VoteUseCase;
import com.ilililissue.vote.domain.entity.Vote;
import com.ilililissue.vote.domain.vo.Stance;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class OpinionController {

    private final OpinionUseCase opinionUseCase;
    private final VoteUseCase voteUseCase;
    private final IssueQuery issueQuery;

    /**
     * 이슈에 대한 내 참여 상태. 비로그인이면 참여하지 않은 것으로 응답한다.
     */
    @GetMapping("/api/issues/{issueId}/participation")
    public ParticipationResponse participation(@PathVariable long issueId,
                                               @LoginMember(required = false) MemberId memberId) {
        IssueId id = new IssueId(issueId);
        issueQuery.getPublicIssue(id);
        if (memberId == null) {
            return new ParticipationResponse(false, null, null);
        }
        Optional<Vote> vote = voteUseCase.findVote(id, memberId);
        Optional<Opinion> opinion = opinionUseCase.findMyOpinion(id, memberId);
        return new ParticipationResponse(
                vote.isPresent(),
                vote.map(Vote::getStance).orElse(null),
                opinion.map(OpinionResponse::from).orElse(null)
        );
    }

    @GetMapping("/api/issues/{issueId}/opinions")
    public PageResult<OpinionItemResponse> opinions(@PathVariable long issueId,
                                                    @LoginMember MemberId memberId,
                                                    @RequestParam Stance stance,
                                                    @RequestParam(defaultValue = "RECOMMENDED") OpinionSort sort,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return opinionUseCase.getOpinions(new IssueId(issueId), memberId, stance, sort, PageQuery.of(page, size))
                .map(OpinionItemResponse::from);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/issues/{issueId}/opinions")
    public OpinionResponse write(@PathVariable long issueId, @LoginMember MemberId memberId,
                                 @RequestBody OpinionRequest request) {
        return OpinionResponse.from(
                opinionUseCase.write(new IssueId(issueId), memberId, OpinionContent.from(request.content())));
    }

    @PutMapping("/api/opinions/{opinionId}")
    public OpinionResponse edit(@PathVariable long opinionId, @LoginMember MemberId memberId,
                                @RequestBody OpinionRequest request) {
        return OpinionResponse.from(
                opinionUseCase.edit(new OpinionId(opinionId), memberId, OpinionContent.from(request.content())));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/opinions/{opinionId}/recommendations")
    public void recommend(@PathVariable long opinionId, @LoginMember MemberId memberId) {
        opinionUseCase.recommend(new OpinionId(opinionId), memberId);
    }

    public record OpinionRequest(String content) {
    }

    public record ParticipationResponse(boolean voted, Stance stance, OpinionResponse opinion) {
    }

    public record OpinionResponse(long id, long issueId, Stance stance, String content, boolean edited,
                                  boolean hidden, LocalDateTime writtenAt) {

        static OpinionResponse from(Opinion opinion) {
            return new OpinionResponse(
                    opinion.getId().getValue(),
                    opinion.getIssueId().getValue(),
                    opinion.getStance(),
                    opinion.getContent().getValue(),
                    opinion.isEdited(),
                    opinion.isHidden(),
                    opinion.getWrittenAt()
            );
        }
    }

    public record OpinionItemResponse(long id, String authorNickname, Stance stance, String content, boolean edited,
                                      long recommendationCount, boolean recommendedByMe, boolean mine,
                                      LocalDateTime writtenAt) {

        static OpinionItemResponse from(OpinionItem item) {
            Opinion opinion = item.opinion();
            return new OpinionItemResponse(
                    opinion.getId().getValue(),
                    item.authorNickname(),
                    opinion.getStance(),
                    opinion.getContent().getValue(),
                    opinion.isEdited(),
                    item.recommendationCount(),
                    item.recommendedByMe(),
                    item.mine(),
                    opinion.getWrittenAt()
            );
        }
    }
}
