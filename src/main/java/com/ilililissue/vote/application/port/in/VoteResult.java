package com.ilililissue.vote.application.port.in;

import com.ilililissue.vote.domain.vo.Stance;

import java.util.EnumMap;
import java.util.Map;

/**
 * 입장별 투표 수. 서비스 참여자 기준 집계이며 대표성 있는 여론조사가 아니다.
 */
public record VoteResult(Map<Stance, Long> counts, long total) {

    public static VoteResult from(Map<Stance, Long> counts) {
        Map<Stance, Long> filled = new EnumMap<>(Stance.class);
        for (Stance stance : Stance.values()) {
            filled.put(stance, counts.getOrDefault(stance, 0L));
        }
        long total = filled.values().stream().mapToLong(Long::longValue).sum();
        return new VoteResult(filled, total);
    }

    /**
     * 소수 첫째 자리까지의 비율(%). 투표가 없으면 0.
     */
    public double percentage(Stance stance) {
        if (total == 0) {
            return 0;
        }
        return Math.round(counts.get(stance) * 1000.0 / total) / 10.0;
    }
}
