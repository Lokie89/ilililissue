package com.ilililissue.issue.domain.entity;

import com.ilililissue.common.SelfValidating;
import com.ilililissue.issue.domain.IllegalIssueStateException;
import com.ilililissue.issue.domain.vo.CategoryId;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.issue.domain.vo.IssueSources;
import com.ilililissue.issue.domain.vo.IssueStatus;
import com.ilililissue.issue.domain.vo.IssueSummary;
import com.ilililissue.issue.domain.vo.IssueTitle;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * <h2>이슈 도메인</h2>
 * <ul>
 * <li>DRAFT → REVIEWED → PUBLISHED → CLOSED 순서로만 전이한다.</li>
 * <li>DRAFT, REVIEWED 상태에서만 내용을 수정할 수 있고, 수정하면 다시 검수가 필요하다(DRAFT).</li>
 * <li>출처가 1개 이상이어야 게시할 수 있다.</li>
 * <li>게시 후 사실관계 정정은 배경 설명만 바꿀 수 있고 정정 시각을 남긴다.</li>
 * <li>게시 중이거나 마감된 이슈는 연장할 수 있다. 연장하면 다시 게시 중이 되고 연장 시각을 남긴다(다음 날 06:59 자동 마감 대상).</li>
 * <li>관리자 권한 검증은 application 계층에서 한다.</li>
 * </ul>
 */
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
public class Issue extends SelfValidating<Issue> {

    private final IssueId id;

    @NotNull
    private IssueTitle title;

    @NotNull
    private IssueSummary summary;

    @NotNull
    private CategoryId categoryId;

    @NotNull
    private IssueSources sources;

    @NotNull
    private IssueStatus status;

    private LocalDateTime publishedAt;

    private LocalDateTime correctedAt;

    private LocalDateTime extendedAt;

    private Issue(
            IssueId id,
            IssueTitle title,
            IssueSummary summary,
            CategoryId categoryId,
            IssueSources sources,
            IssueStatus status,
            LocalDateTime publishedAt,
            LocalDateTime correctedAt,
            LocalDateTime extendedAt
    ) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.categoryId = categoryId;
        this.sources = sources;
        this.status = status;
        this.publishedAt = publishedAt;
        this.correctedAt = correctedAt;
        this.extendedAt = extendedAt;
        this.validate();
    }

    public static Issue draft(IssueTitle title, IssueSummary summary, CategoryId categoryId, IssueSources sources) {
        return new Issue(null, title, summary, categoryId, sources, IssueStatus.DRAFT, null, null, null);
    }

    public static Issue withId(IssueId id, IssueTitle title, IssueSummary summary, CategoryId categoryId,
                               IssueSources sources, IssueStatus status,
                               LocalDateTime publishedAt, LocalDateTime correctedAt, LocalDateTime extendedAt) {
        return new Issue(id, title, summary, categoryId, sources, status, publishedAt, correctedAt, extendedAt);
    }

    public void edit(IssueTitle title, IssueSummary summary, CategoryId categoryId, IssueSources sources) {
        requireStatusIn("수정", IssueStatus.DRAFT, IssueStatus.REVIEWED);
        this.title = title;
        this.summary = summary;
        this.categoryId = categoryId;
        this.sources = sources;
        this.status = IssueStatus.DRAFT;
        this.validate();
    }

    public void review() {
        requireStatusIn("검수", IssueStatus.DRAFT);
        this.status = IssueStatus.REVIEWED;
    }

    public void publish(LocalDateTime now) {
        requireStatusIn("게시", IssueStatus.REVIEWED);
        if (this.sources.isEmpty()) {
            throw new IllegalIssueStateException("출처가 없는 이슈는 게시할 수 없습니다.");
        }
        this.status = IssueStatus.PUBLISHED;
        this.publishedAt = now;
    }

    public void close() {
        requireStatusIn("마감", IssueStatus.PUBLISHED);
        this.status = IssueStatus.CLOSED;
    }

    /**
     * 투표 기간을 연장한다. 마감된 이슈는 다시 열린다.
     */
    public void extend(LocalDateTime now) {
        requireStatusIn("연장", IssueStatus.PUBLISHED, IssueStatus.CLOSED);
        this.status = IssueStatus.PUBLISHED;
        this.extendedAt = now;
    }

    public void correct(IssueSummary correctedSummary, LocalDateTime now) {
        requireStatusIn("정정", IssueStatus.PUBLISHED, IssueStatus.CLOSED);
        this.summary = correctedSummary;
        this.correctedAt = now;
        this.validate();
    }

    public boolean isOpenForVoting() {
        return this.status == IssueStatus.PUBLISHED;
    }

    public boolean isCorrected() {
        return this.correctedAt != null;
    }

    private void requireStatusIn(String action, IssueStatus... allowed) {
        for (IssueStatus status : allowed) {
            if (this.status == status) {
                return;
            }
        }
        throw new IllegalIssueStateException(this.status, action);
    }
}
