package com.ilililissue.newsdraft.application.port.out;

import com.ilililissue.newsdraft.domain.NewsArticle;

import java.util.List;

public interface NewsSearchPort {

    /**
     * 검색어로 최신 기사를 최신순으로 가져온다.
     */
    List<NewsArticle> searchLatest(String keyword, int count);
}
