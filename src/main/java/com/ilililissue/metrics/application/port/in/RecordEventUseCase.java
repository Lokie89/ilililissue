package com.ilililissue.metrics.application.port.in;

import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.vote.domain.vo.Stance;

public interface RecordEventUseCase {

    void issueViewed(IssueId issueId, MemberId memberIdOrNull);

    void voted(IssueId issueId, MemberId memberId, Stance stance);

    void opinionWritten(IssueId issueId, MemberId memberId, Stance stance);

    void opinionsViewed(IssueId issueId, MemberId viewerId, Stance viewerStance, Stance tabStance);
}
