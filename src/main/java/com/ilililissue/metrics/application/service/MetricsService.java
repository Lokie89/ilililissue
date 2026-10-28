package com.ilililissue.metrics.application.service;

import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.metrics.application.port.in.MetricsQuery;
import com.ilililissue.metrics.application.port.in.ParticipationMetrics;
import com.ilililissue.metrics.application.port.in.RecordEventUseCase;
import com.ilililissue.metrics.application.port.out.ParticipationEventPort;
import com.ilililissue.metrics.application.port.out.ParticipationEventPort.MemberActivity;
import com.ilililissue.metrics.domain.ParticipationEvent;
import com.ilililissue.metrics.domain.ParticipationEventType;
import com.ilililissue.vote.domain.vo.Stance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MetricsService implements RecordEventUseCase, MetricsQuery {

    private static final int REVISIT_DAYS = 7;

    private final ParticipationEventPort eventPort;
    private final Clock clock;

    @Transactional
    @Override
    public void issueViewed(IssueId issueId, MemberId memberIdOrNull) {
        record(ParticipationEventType.ISSUE_VIEWED, issueId, memberIdOrNull, null, null);
    }

    @Transactional
    @Override
    public void voted(IssueId issueId, MemberId memberId, Stance stance) {
        record(ParticipationEventType.VOTED, issueId, memberId, stance, null);
    }

    @Transactional
    @Override
    public void opinionWritten(IssueId issueId, MemberId memberId, Stance stance) {
        record(ParticipationEventType.OPINION_WRITTEN, issueId, memberId, stance, null);
    }

    @Transactional
    @Override
    public void opinionsViewed(IssueId issueId, MemberId viewerId, Stance viewerStance, Stance tabStance) {
        record(ParticipationEventType.OPINIONS_VIEWED, issueId, viewerId, viewerStance, tabStance);
    }

    @Transactional(readOnly = true)
    @Override
    public ParticipationMetrics getMetrics(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        long issueViews = eventPort.count(ParticipationEventType.ISSUE_VIEWED, start, end);
        long votes = eventPort.count(ParticipationEventType.VOTED, start, end);
        long opinions = eventPort.count(ParticipationEventType.OPINION_WRITTEN, start, end);
        long voters = eventPort.countDistinctMembers(ParticipationEventType.VOTED, start, end);
        long opposingViewers = eventPort.countDistinctOpposingViewers(start, end);

        LocalDateTime cohortEnd = end.minusDays(REVISIT_DAYS);
        List<MemberActivity> cohort = eventPort.findMemberActivities(end).stream()
                .filter(activity -> !activity.firstAt().isBefore(start) && activity.firstAt().isBefore(cohortEnd))
                .toList();
        long revisited = cohort.stream()
                .filter(activity -> !activity.lastAt().isBefore(activity.firstAt().plusDays(REVISIT_DAYS)))
                .count();

        return new ParticipationMetrics(
                from, to,
                issueViews, votes, opinions, voters, opposingViewers,
                ParticipationMetrics.rate(votes, issueViews),
                ParticipationMetrics.rate(opinions, votes),
                ParticipationMetrics.rate(opposingViewers, voters),
                cohort.size(),
                ParticipationMetrics.rate(revisited, cohort.size())
        );
    }

    private void record(ParticipationEventType type, IssueId issueId, MemberId memberId, Stance stance,
                        Stance targetStance) {
        eventPort.save(new ParticipationEvent(type, issueId, memberId, stance, targetStance,
                LocalDateTime.now(clock)));
    }
}
