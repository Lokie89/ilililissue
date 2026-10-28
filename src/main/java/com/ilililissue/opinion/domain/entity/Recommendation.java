package com.ilililissue.opinion.domain.entity;

import com.ilililissue.common.SelfValidating;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.opinion.domain.vo.OpinionId;
import com.ilililissue.opinion.domain.vo.RecommendationId;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * <h2>추천 도메인</h2>
 * <ul>
 * <li>의견에 대한 단방향 공감이며 비추천은 없다.</li>
 * <li>회원당 의견별 1회이며 저장소의 (opinionId, memberId) 유니크 제약으로 보장한다.</li>
 * </ul>
 * 추천 취소, 자기 의견 추천 허용 여부는 미정이다.
 */
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
public class Recommendation extends SelfValidating<Recommendation> {

    private final RecommendationId id;

    @NotNull
    private final OpinionId opinionId;

    @NotNull
    private final MemberId memberId;

    @NotNull
    private final LocalDateTime recommendedAt;

    private Recommendation(
            RecommendationId id,
            OpinionId opinionId,
            MemberId memberId,
            LocalDateTime recommendedAt
    ) {
        this.id = id;
        this.opinionId = opinionId;
        this.memberId = memberId;
        this.recommendedAt = recommendedAt;
        this.validate();
    }

    public static Recommendation of(OpinionId opinionId, MemberId memberId, LocalDateTime now) {
        return new Recommendation(null, opinionId, memberId, now);
    }

    public static Recommendation withId(RecommendationId id, OpinionId opinionId, MemberId memberId,
                                        LocalDateTime recommendedAt) {
        return new Recommendation(id, opinionId, memberId, recommendedAt);
    }
}
