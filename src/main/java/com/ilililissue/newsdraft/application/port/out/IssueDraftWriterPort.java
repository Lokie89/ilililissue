package com.ilililissue.newsdraft.application.port.out;

import com.ilililissue.issue.domain.entity.Category;
import com.ilililissue.newsdraft.domain.IssueDraft;
import com.ilililissue.newsdraft.domain.NewsArticle;

import java.util.List;

public interface IssueDraftWriterPort {

    /**
     * 수집한 기사에서 토론할 만한 이슈를 골라 초안을 쓴다.
     *
     * @param recentTitles 최근 이슈 질문. 같은 주제를 다시 만들지 않도록 넘긴다
     */
    List<IssueDraft> write(List<NewsArticle> articles, List<Category> categories, List<String> recentTitles, int maxDrafts);
}
