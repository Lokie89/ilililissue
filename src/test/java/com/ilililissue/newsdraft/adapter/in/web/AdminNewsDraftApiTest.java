package com.ilililissue.newsdraft.adapter.in.web;

import com.ilililissue.support.ApiTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminNewsDraftApiTest extends ApiTestSupport {

    @DisplayName("뉴스 초안 실행·상태 조회는 관리자만 할 수 있다")
    @Test
    void adminOnly() throws Exception {
        mockMvc.perform(post("/api/admin/news-drafts")).andExpect(status().isUnauthorized());
        Cookie member = loginAsNewUser();
        mockMvc.perform(post("/api/admin/news-drafts").cookie(member)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/news-drafts/status").cookie(member)).andExpect(status().isForbidden());
    }

    @DisplayName("관리자는 실행 상태를 조회할 수 있다")
    @Test
    void runStatus() throws Exception {
        Cookie admin = loginAsNewAdmin();
        mockMvc.perform(get("/api/admin/news-drafts/status").cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").isBoolean());
    }
}
