package com.ilililissue.issue.application.port.in;

public interface CloseExpiredIssuesUseCase {

    /**
     * 오늘(Asia/Seoul) 0시 전에 게시(연장했으면 연장)된 게시 중 이슈를 모두 마감한다.
     *
     * @return 마감한 이슈 수
     */
    int closePublishedBeforeToday();
}
