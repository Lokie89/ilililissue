package com.ilililissue.metrics.application.port.in;

import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.vote.domain.vo.Stance;

public interface RecordEventUseCase {

    /**
     * @param visitorId 브라우저(기기)별 방문자 식별자. 같은 방문자의 같은 이슈 조회는 지표에서 한 번으로 센다.
     */
    void issueViewed(IssueId issueId, MemberId memberIdOrNull, String visitorId);

    void voted(IssueId issueId, MemberId memberId, Stance stance);

    void opinionWritten(IssueId issueId, MemberId memberId, Stance stance);

    void opinionsViewed(IssueId issueId, MemberId viewerId, Stance viewerStance, Stance tabStance);
}
