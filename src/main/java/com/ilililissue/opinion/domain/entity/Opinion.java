package com.ilililissue.opinion.domain.entity;

import com.ilililissue.common.SelfValidating;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.opinion.domain.AlreadyEditedOpinionException;
import com.ilililissue.opinion.domain.NotOpinionAuthorException;
import com.ilililissue.opinion.domain.vo.OpinionContent;
import com.ilililissue.opinion.domain.vo.OpinionId;
import com.ilililissue.vote.domain.entity.Vote;
import com.ilililissue.vote.domain.vo.Stance;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * <h2>의견 도메인</h2>
 * <ul>
 * <li>투표한 회원만 작성할 수 있고, 의견의 입장은 투표 입장과 같다.</li>
 * <li>회원당 이슈별 1개이며 저장소의 (issueId, authorId) 유니크 제약으로 보장한다.</li>
 * <li>작성자만 최대 1회 수정할 수 있다.</li>
 * <li>작성자는 의견을 삭제할 수 있다. 삭제해도 같은 이슈에 다시 작성할 수 없고, 삭제한 의견은 수정할 수 없다.</li>
 * <li>다른 의견에 대한 답글이 아닌 독립된 글이다.</li>
 * </ul>
 */
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
public class Opinion extends SelfValidating<Opinion> {

    private final OpinionId id;

    @NotNull
    private final IssueId issueId;

    @NotNull
    private final MemberId authorId;

    @NotNull
    private final Stance stance;

    @NotNull
    private OpinionContent content;

    private boolean edited;

    private boolean hidden;

    private boolean deleted;

    @NotNull
    private final LocalDateTime writtenAt;

    private Opinion(
            OpinionId id,
            IssueId issueId,
            MemberId authorId,
            Stance stance,
            OpinionContent content,
            boolean edited,
            boolean hidden,
            boolean deleted,
            LocalDateTime writtenAt
    ) {
        this.id = id;
        this.issueId = issueId;
        this.authorId = authorId;
        this.stance = stance;
        this.content = content;
        this.edited = edited;
        this.hidden = hidden;
        this.deleted = deleted;
        this.writtenAt = writtenAt;
        this.validate();
    }

    public static Opinion write(Vote vote, OpinionContent content, LocalDateTime now) {
        return new Opinion(null, vote.getIssueId(), vote.getMemberId(), vote.getStance(), content, false, false, false, now);
    }

    public static Opinion withId(OpinionId id, IssueId issueId, MemberId authorId, Stance stance,
                                 OpinionContent content, boolean edited, boolean hidden, boolean deleted,
                                 LocalDateTime writtenAt) {
        return new Opinion(id, issueId, authorId, stance, content, edited, hidden, deleted, writtenAt);
    }

    public void edit(MemberId editorId, OpinionContent content) {
        requireAuthor(editorId);
        if (this.edited) {
            throw new AlreadyEditedOpinionException();
        }
        this.content = content;
        this.edited = true;
        this.validate();
    }

    public void delete(MemberId memberId) {
        requireAuthor(memberId);
        this.deleted = true;
    }

    public boolean isWrittenBy(MemberId memberId) {
        return this.authorId.equals(memberId);
    }

    public void hide() {
        this.hidden = true;
    }

    public void unhide() {
        this.hidden = false;
    }

    private void requireAuthor(MemberId memberId) {
        if (!isWrittenBy(memberId)) {
            throw new NotOpinionAuthorException();
        }
    }
}
