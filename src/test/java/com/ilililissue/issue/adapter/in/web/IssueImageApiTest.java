package com.ilililissue.issue.adapter.in.web;

import com.ilililissue.issue.application.port.in.ManageIssueUseCase;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.support.ApiTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IssueImageApiTest extends ApiTestSupport {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, 1, 2, 3};

    @Autowired
    ManageIssueUseCase manageIssueUseCase;

    @DisplayName("게시된 이슈의 대표 이미지는 누구나 보고, 게시 전 이미지는 관리자만 본다")
    @Test
    void imageVisibility() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long published = createPublishedIssue(admin);
        long draft = createDraftIssue(admin, createCategory(admin));
        manageIssueUseCase.attachImage(new IssueId(published), JPEG);
        manageIssueUseCase.attachImage(new IssueId(draft), JPEG);

        mockMvc.perform(get("/api/issues/{id}", published))
                .andExpect(jsonPath("$.imageKey").isNotEmpty());
        mockMvc.perform(get("/api/issues/{id}/image", published))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(JPEG));
        mockMvc.perform(get("/api/issues/{id}/image", draft)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/admin/issues/{id}/image", draft).cookie(admin)).andExpect(status().isOk());
    }

    @DisplayName("관리자가 이미지를 지우면 더 이상 보이지 않는다")
    @Test
    void removeImage() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long published = createPublishedIssue(admin);
        manageIssueUseCase.attachImage(new IssueId(published), JPEG);

        mockMvc.perform(delete("/api/admin/issues/{id}/image", published).cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageKey").doesNotExist());
        mockMvc.perform(get("/api/issues/{id}/image", published)).andExpect(status().isNotFound());
    }

    @DisplayName("이슈가 이미지가 없으면 404, 기본 설정(OpenAI API)에서는 이미지 생성을 설정 오류로 거절한다")
    @Test
    void noImage() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long published = createPublishedIssue(admin);

        mockMvc.perform(get("/api/issues/{id}/image", published)).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/admin/issues/{id}/image", published).cookie(admin)).andExpect(status().isConflict());
        mockMvc.perform(post("/api/admin/issues/{id}/image", published).cookie(loginAsNewUser()))
                .andExpect(status().isForbidden());
    }
}
