package com.ilililissue.newsdraft.application.port.out;

import com.ilililissue.newsdraft.config.NewsDraftProperties.Feed;
import com.ilililissue.newsdraft.domain.NewsArticle;

import java.util.List;

public interface NewsFeedPort {

    /**
     * 피드의 기사를 피드 순서대로 가져온다. 읽지 못하면 예외를 던진다.
     */
    List<NewsArticle> fetch(Feed feed);
}
