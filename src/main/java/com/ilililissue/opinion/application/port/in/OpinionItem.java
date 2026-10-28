package com.ilililissue.opinion.application.port.in;

import com.ilililissue.opinion.domain.entity.Opinion;

/**
 * 입장별 의견 목록의 한 항목.
 */
public record OpinionItem(
        Opinion opinion,
        String authorNickname,
        long recommendationCount,
        boolean recommendedByMe,
        boolean mine
) {
}
