package com.ilililissue.issue.domain.entity.dto;

import com.ilililissue.issue.domain.vo.IssueImages;
import com.ilililissue.issue.domain.vo.User;

/**
 * @author SeongRok.Oh
 * @since 2023/08/21
 */
public record UpdateIssueDto(IssueImages images, User register) {

    public static UpdateIssueDto from(IssueImages updateImages, User register) {
        return new UpdateIssueDto(updateImages, register);
    }
}
