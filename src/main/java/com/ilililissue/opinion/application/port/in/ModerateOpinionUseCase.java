package com.ilililissue.opinion.application.port.in;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.opinion.domain.entity.Opinion;
import com.ilililissue.opinion.domain.vo.OpinionId;

/**
 * 신고·관리자 조치에서 쓰는 의견 관리 기능.
 */
public interface ModerateOpinionUseCase {

    Opinion getOpinion(OpinionId opinionId);

    void hide(OpinionId opinionId);

    /**
     * 숨김 처리를 되돌린다. 이미 처리된 신고의 상태는 기록으로 남긴다.
     */
    void unhide(OpinionId opinionId);

    /**
     * 숨김 처리된 의견을 최신순으로 조회한다. 작성자가 삭제한 의견은 제외한다.
     */
    PageResult<Opinion> getHiddenOpinions(PageQuery pageQuery);
}
