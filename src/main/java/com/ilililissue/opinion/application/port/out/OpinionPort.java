package com.ilililissue.opinion.application.port.out;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.opinion.application.port.in.OpinionSort;
import com.ilililissue.opinion.domain.entity.Opinion;
import com.ilililissue.opinion.domain.vo.OpinionId;
import com.ilililissue.vote.domain.vo.Stance;

import java.util.Optional;

public interface OpinionPort {

    Opinion save(Opinion opinion);

    Optional<Opinion> findById(OpinionId id);

    Optional<Opinion> findByIssueIdAndAuthorId(IssueId issueId, MemberId authorId);

    /**
     * 숨김·삭제되지 않은 의견을 입장별로 조회한다.
     */
    PageResult<CountedOpinion> findVisible(IssueId issueId, Stance stance, OpinionSort sort, PageQuery pageQuery);

    void incrementRecommendationCount(OpinionId id);

    void decrementRecommendationCount(OpinionId id);

    record CountedOpinion(Opinion opinion, long recommendationCount) {
    }
}
