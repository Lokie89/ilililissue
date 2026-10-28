package com.ilililissue.metrics.application.port.in;

import java.time.LocalDate;

/**
 * 기간 내 참여 지표. 비율은 0~100(%)이며 분모가 0이면 0이다.
 *
 * @param issueViews          이슈 상세 조회 수 (비회원 포함, 중복 포함)
 * @param votes               투표 수
 * @param opinions            의견 작성 수
 * @param opposingViewers     자신과 다른 입장의 의견 탭을 연 투표자 수 (중복 제외)
 * @param voteRate            투표 수 / 이슈 조회 수
 * @param opinionRate         의견 작성 수 / 투표 수
 * @param opposingViewRate    다른 입장 탭을 연 투표자 수 / 기간 내 투표자 수
 * @param revisitCohort       기간 시작 ~ 기간 끝 7일 전에 처음 활동한 회원 수
 * @param revisit7dRate       revisitCohort 중 첫 활동 7일 이후에 다시 활동한 회원 비율
 */
public record ParticipationMetrics(
        LocalDate from,
        LocalDate to,
        long issueViews,
        long votes,
        long opinions,
        long voters,
        long opposingViewers,
        double voteRate,
        double opinionRate,
        double opposingViewRate,
        long revisitCohort,
        double revisit7dRate
) {

    public static double rate(long numerator, long denominator) {
        if (denominator == 0) {
            return 0;
        }
        return Math.round(numerator * 1000.0 / denominator) / 10.0;
    }
}
