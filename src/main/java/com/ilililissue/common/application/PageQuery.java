package com.ilililissue.common.application;

/**
 * 0부터 시작하는 페이지 요청. size는 1~50으로 보정한다.
 */
public record PageQuery(int page, int size) {

    private static final int MAX_SIZE = 50;

    public PageQuery {
        page = Math.max(page, 0);
        size = Math.min(Math.max(size, 1), MAX_SIZE);
    }

    public static PageQuery of(int page, int size) {
        return new PageQuery(page, size);
    }
}
