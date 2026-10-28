package com.ilililissue.opinion.application.port.in;

import com.ilililissue.opinion.domain.entity.Opinion;
import com.ilililissue.opinion.domain.vo.OpinionId;

/**
 * 신고·관리자 조치에서 쓰는 의견 관리 기능.
 */
public interface ModerateOpinionUseCase {

    Opinion getOpinion(OpinionId opinionId);

    void hide(OpinionId opinionId);
}
