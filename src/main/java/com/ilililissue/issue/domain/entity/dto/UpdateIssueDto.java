package com.ilililissue.issue.domain.entity.dto;

import com.ilililissue.issue.domain.vo.User;

/**
 * @author SeongRok.Oh
 * @since 2023/08/21
 */
public record UpdateIssueDto(String image, User register) {

    public static UpdateIssueDto from(String updateImage, User register) {
        return new UpdateIssueDto(updateImage, register);
    }
}
