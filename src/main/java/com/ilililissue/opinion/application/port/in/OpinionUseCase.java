package com.ilililissue.opinion.application.port.in;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.opinion.domain.entity.Opinion;
import com.ilililissue.opinion.domain.vo.OpinionContent;
import com.ilililissue.opinion.domain.vo.OpinionId;
import com.ilililissue.vote.domain.vo.Stance;

import java.util.Optional;

public interface OpinionUseCase {

    Opinion write(IssueId issueId, MemberId authorId, OpinionContent content);

    Opinion edit(OpinionId opinionId, MemberId editorId, OpinionContent content);

    /**
     * 작성자가 의견을 삭제한다. 삭제한 뒤에는 같은 이슈에 의견을 다시 작성할 수 없다.
     */
    void delete(OpinionId opinionId, MemberId memberId);

    void recommend(OpinionId opinionId, MemberId memberId);

    /**
     * 추천을 취소한다. 추천하지 않은 의견이면 아무 일도 하지 않는다.
     */
    void cancelRecommendation(OpinionId opinionId, MemberId memberId);

    /**
     * 투표한 회원에게만 입장별 의견 목록을 공개한다. 숨김 처리된 의견은 제외한다.
     */
    PageResult<OpinionItem> getOpinions(IssueId issueId, MemberId viewerId, Stance stance, OpinionSort sort,
                                        PageQuery pageQuery);

    Optional<Opinion> findMyOpinion(IssueId issueId, MemberId memberId);
}
