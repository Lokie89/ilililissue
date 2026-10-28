package com.ilililissue.issue.application;

import com.ilililissue.common.DomainException;

public class CategoryException extends DomainException {

    private CategoryException(ErrorType type, String message) {
        super(type, message);
    }

    public static CategoryException notFound() {
        return new CategoryException(ErrorType.NOT_FOUND, "카테고리를 찾을 수 없습니다.");
    }

    public static CategoryException duplicate() {
        return new CategoryException(ErrorType.CONFLICT, "이미 있는 카테고리입니다.");
    }
}
