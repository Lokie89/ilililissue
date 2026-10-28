package com.ilililissue.opinion.application.port.out;

import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.opinion.domain.entity.Recommendation;
import com.ilililissue.opinion.domain.vo.OpinionId;

import java.util.Collection;
import java.util.Set;

public interface RecommendationPort {

    void save(Recommendation recommendation);

    boolean exists(OpinionId opinionId, MemberId memberId);

    /**
     * @return 삭제한 추천이 있으면 true
     */
    boolean delete(OpinionId opinionId, MemberId memberId);

    Set<OpinionId> findRecommendedOpinionIds(MemberId memberId, Collection<OpinionId> opinionIds);
}
