package com.ilililissue.member.adapter.in.web;

import com.ilililissue.auth.adapter.in.web.AuthCookies;
import com.ilililissue.support.ApiTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MemberWithdrawalApiTest extends ApiTestSupport {

    @DisplayName("탈퇴하면 로그인 정보가 무효가 되고, 의견은 '탈퇴한 회원'으로 남으며, 같은 이메일로 다시 가입할 수 있다")
    @Test
    void withdraw() throws Exception {
        long issueId = createPublishedIssue(loginAsNewAdmin());
        String email = "leaver-" + unique() + "@example.com";
        signup(email);
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        Cookie access = login.getResponse().getCookie(AuthCookies.ACCESS_TOKEN);
        Cookie refresh = login.getResponse().getCookie(AuthCookies.REFRESH_TOKEN);

        mockMvc.perform(post("/api/issues/{id}/votes", issueId).cookie(access)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"stance\":\"AGREE\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/issues/{id}/opinions", issueId).cookie(access)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"남겨질 의견\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/members/me/withdrawal")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"%s\"}".formatted(PASSWORD)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/members/me/withdrawal").cookie(access)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"wrong-password\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/members/me/withdrawal").cookie(access)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"%s\"}".formatted(PASSWORD)))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(AuthCookies.ACCESS_TOKEN, 0))
                .andExpect(cookie().maxAge(AuthCookies.REFRESH_TOKEN, 0));

        mockMvc.perform(get("/api/members/me").cookie(access))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").cookie(refresh))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD)))
                .andExpect(status().isUnauthorized());

        Cookie viewer = loginAsNewUser();
        mockMvc.perform(post("/api/issues/{id}/votes", issueId).cookie(viewer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"stance\":\"DISAGREE\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(viewer).param("stance", "AGREE"))
                .andExpect(jsonPath("$.content[0].content").value("남겨질 의견"))
                .andExpect(jsonPath("$.content[0].authorNickname").value("탈퇴한 회원"));
        mockMvc.perform(get("/api/issues/{id}/results", issueId).cookie(viewer))
                .andExpect(jsonPath("$.total").value(2));

        signup(email);
    }
}
