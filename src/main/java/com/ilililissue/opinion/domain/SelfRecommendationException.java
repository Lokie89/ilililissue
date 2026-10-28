package com.ilililissue.opinion.domain;

import com.ilililissue.common.DomainException;

public class SelfRecommendationException extends DomainException {

    private static final String message = "자기 의견은 추천할 수 없습니다.";

    public SelfRecommendationException() {
        super(ErrorType.FORBIDDEN, message);
    }
}
