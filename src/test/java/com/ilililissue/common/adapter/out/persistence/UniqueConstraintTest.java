package com.ilililissue.common.adapter.out.persistence;

import com.ilililissue.opinion.adapter.out.persistence.OpinionJpaEntity;
import com.ilililissue.opinion.adapter.out.persistence.OpinionJpaRepository;
import com.ilililissue.opinion.adapter.out.persistence.RecommendationJpaEntity;
import com.ilililissue.opinion.adapter.out.persistence.RecommendationJpaRepository;
import com.ilililissue.vote.adapter.out.persistence.VoteJpaEntity;
import com.ilililissue.vote.adapter.out.persistence.VoteJpaRepository;
import com.ilililissue.vote.domain.vo.Stance;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

/**
 * 애플리케이션 검사를 동시에 통과한 요청도 "회원당 1회" 제약을 넘지 못하도록 DB 제약을 검증한다.
 */
@DataJpaTest
public class UniqueConstraintTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 28, 9, 0);

    @Autowired
    VoteJpaRepository voteJpaRepository;

    @Autowired
    OpinionJpaRepository opinionJpaRepository;

    @Autowired
    RecommendationJpaRepository recommendationJpaRepository;

    @DisplayName("회원당 이슈별 투표 1개")
    @Test
    void vote() {
        voteJpaRepository.saveAndFlush(new VoteJpaEntity(null, 1L, 1L, Stance.AGREE, NOW));

        Assertions.assertThatThrownBy(() ->
                        voteJpaRepository.saveAndFlush(new VoteJpaEntity(null, 1L, 1L, Stance.DISAGREE, NOW)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("회원당 이슈별 의견 1개")
    @Test
    void opinion() {
        opinionJpaRepository.saveAndFlush(
                new OpinionJpaEntity(null, 1L, 1L, Stance.AGREE, "의견", false, false, false, 0, NOW));

        Assertions.assertThatThrownBy(() -> opinionJpaRepository.saveAndFlush(
                        new OpinionJpaEntity(null, 1L, 1L, Stance.AGREE, "또 의견", false, false, false, 0, NOW)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("회원당 의견별 추천 1개")
    @Test
    void recommendation() {
        recommendationJpaRepository.saveAndFlush(new RecommendationJpaEntity(null, 1L, 1L, NOW));

        Assertions.assertThatThrownBy(() ->
                        recommendationJpaRepository.saveAndFlush(new RecommendationJpaEntity(null, 1L, 1L, NOW)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
