package com.ilililissue.opinion.application.service;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.issue.application.port.in.IssueQuery;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.application.port.out.LoadMemberPort;
import com.ilililissue.member.domain.entity.Member;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.member.domain.vo.Nickname;
import com.ilililissue.metrics.application.port.in.RecordEventUseCase;
import com.ilililissue.opinion.application.OpinionException;
import com.ilililissue.opinion.application.port.in.ModerateOpinionUseCase;
import com.ilililissue.opinion.application.port.in.OpinionItem;
import com.ilililissue.opinion.application.port.in.OpinionSort;
import com.ilililissue.opinion.application.port.in.OpinionUseCase;
import com.ilililissue.opinion.application.port.out.OpinionPort;
import com.ilililissue.opinion.application.port.out.OpinionPort.CountedOpinion;
import com.ilililissue.opinion.application.port.out.RecommendationPort;
import com.ilililissue.opinion.domain.entity.Opinion;
import com.ilililissue.opinion.domain.entity.Recommendation;
import com.ilililissue.opinion.domain.vo.OpinionContent;
import com.ilililissue.opinion.domain.vo.OpinionId;
import com.ilililissue.vote.application.port.in.VoteUseCase;
import com.ilililissue.vote.domain.entity.Vote;
import com.ilililissue.vote.domain.vo.Stance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class OpinionService implements OpinionUseCase, ModerateOpinionUseCase {

    private final IssueQuery issueQuery;
    private final VoteUseCase voteUseCase;
    private final OpinionPort opinionPort;
    private final RecommendationPort recommendationPort;
    private final LoadMemberPort loadMemberPort;
    private final RecordEventUseCase recordEventUseCase;
    private final Clock clock;

    @Transactional
    @Override
    public Opinion write(IssueId issueId, MemberId authorId, OpinionContent content) {
        issueQuery.getPublicIssue(issueId);
        Vote vote = voteUseCase.requireVote(issueId, authorId);
        Optional<Opinion> existing = opinionPort.findByIssueIdAndAuthorId(issueId, authorId);
        if (existing.isPresent()) {
            throw existing.get().isDeleted()
                    ? OpinionException.deletedCannotRewrite()
                    : OpinionException.alreadyWritten();
        }
        Opinion opinion = opinionPort.save(Opinion.write(vote, content, now()));
        recordEventUseCase.opinionWritten(issueId, authorId, opinion.getStance());
        return opinion;
    }

    @Transactional
    @Override
    public Opinion edit(OpinionId opinionId, MemberId editorId, OpinionContent content) {
        Opinion opinion = loadNotDeleted(opinionId);
        opinion.edit(editorId, content);
        return opinionPort.save(opinion);
    }

    @Transactional
    @Override
    public void delete(OpinionId opinionId, MemberId memberId) {
        Opinion opinion = loadNotDeleted(opinionId);
        opinion.delete(memberId);
        opinionPort.save(opinion);
    }

    @Transactional
    @Override
    public void recommend(OpinionId opinionId, MemberId memberId) {
        Opinion opinion = loadNotDeleted(opinionId);
        if (opinion.isHidden()) {
            throw OpinionException.notFound();
        }
        voteUseCase.requireVote(opinion.getIssueId(), memberId);
        Recommendation recommendation = Recommendation.of(opinion, memberId, now());
        if (recommendationPort.exists(opinionId, memberId)) {
            throw OpinionException.alreadyRecommended();
        }
        recommendationPort.save(recommendation);
        opinionPort.incrementRecommendationCount(opinionId);
    }

    @Transactional
    @Override
    public void cancelRecommendation(OpinionId opinionId, MemberId memberId) {
        opinionPort.findById(opinionId).orElseThrow(OpinionException::notFound);
        // 동시에 들어온 취소 요청 중 실제로 지운 요청만 추천 수를 줄인다.
        if (recommendationPort.delete(opinionId, memberId)) {
            opinionPort.decrementRecommendationCount(opinionId);
        }
    }

    // 탭 열람 이벤트를 기록하므로 읽기 전용 트랜잭션을 쓰지 않는다.
    @Transactional
    @Override
    public PageResult<OpinionItem> getOpinions(IssueId issueId, MemberId viewerId, Stance stance, OpinionSort sort,
                                               PageQuery pageQuery) {
        issueQuery.getPublicIssue(issueId);
        Vote viewerVote = voteUseCase.requireVote(issueId, viewerId);
        if (pageQuery.page() == 0) {
            recordEventUseCase.opinionsViewed(issueId, viewerId, viewerVote.getStance(), stance);
        }

        PageResult<CountedOpinion> page = opinionPort.findVisible(issueId, stance, sort, pageQuery);
        List<Opinion> opinions = page.content().stream().map(CountedOpinion::opinion).toList();
        Map<MemberId, Nickname> nicknames = loadMemberPort.findNicknames(
                opinions.stream().map(Opinion::getAuthorId).toList());
        Set<OpinionId> recommended = recommendationPort.findRecommendedOpinionIds(
                viewerId, opinions.stream().map(Opinion::getId).toList());

        return page.map(counted -> {
            Opinion opinion = counted.opinion();
            Nickname nickname = nicknames.get(opinion.getAuthorId());
            return new OpinionItem(
                    opinion,
                    nickname == null ? Member.WITHDRAWN_NICKNAME : nickname.getValue(),
                    counted.recommendationCount(),
                    recommended.contains(opinion.getId()),
                    opinion.getAuthorId().equals(viewerId)
            );
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Opinion> findMyOpinion(IssueId issueId, MemberId memberId) {
        return opinionPort.findByIssueIdAndAuthorId(issueId, memberId);
    }

    @Transactional(readOnly = true)
    @Override
    public Opinion getOpinion(OpinionId opinionId) {
        return opinionPort.findById(opinionId).orElseThrow(OpinionException::notFound);
    }

    @Transactional
    @Override
    public void hide(OpinionId opinionId) {
        Opinion opinion = getOpinion(opinionId);
        opinion.hide();
        opinionPort.save(opinion);
    }

    @Transactional
    @Override
    public void unhide(OpinionId opinionId) {
        Opinion opinion = getOpinion(opinionId);
        opinion.unhide();
        opinionPort.save(opinion);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<Opinion> getHiddenOpinions(PageQuery pageQuery) {
        return opinionPort.findHidden(pageQuery);
    }

    private Opinion loadNotDeleted(OpinionId opinionId) {
        return opinionPort.findById(opinionId)
                .filter(opinion -> !opinion.isDeleted())
                .orElseThrow(OpinionException::notFound);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
