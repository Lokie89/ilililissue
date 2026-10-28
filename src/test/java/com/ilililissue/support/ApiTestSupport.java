package com.ilililissue.support;

import com.ilililissue.auth.adapter.in.web.AuthCookies;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API 통합 테스트 공통 기능: 회원 가입·로그인, 관리자 지정, 게시된 이슈 준비.
 * 테스트끼리 데이터를 공유하므로 이메일·닉네임·카테고리 이름은 매번 고유하게 만든다.
 */
@AutoConfigureMockMvc
@SpringBootTest
public abstract class ApiTestSupport {

    protected static final String PASSWORD = "password1234";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected static String unique() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    protected Cookie loginAsNewUser() throws Exception {
        String email = "user-" + unique() + "@example.com";
        signup(email);
        return login(email);
    }

    protected Cookie loginAsNewAdmin() throws Exception {
        String email = "admin-" + unique() + "@example.com";
        signup(email);
        jdbcTemplate.update("UPDATE member SET role = 'ADMIN' WHERE email = ?", email);
        return login(email);
    }

    protected void signup(String email) throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","nickname":"%s","password":"%s","agreedToTerms":true}
                                """.formatted(email, "닉" + unique(), PASSWORD)))
                .andExpect(status().isCreated());
    }

    protected Cookie login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie(AuthCookies.ACCESS_TOKEN);
    }

    protected long createCategory(Cookie admin) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/categories")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s"}
                                """.formatted("분류" + unique())))
                .andExpect(status().isCreated())
                .andReturn();
        return readLong(result, "$.id");
    }

    protected long createDraftIssue(Cookie admin, long categoryId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/issues")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(issueJson("주 4일제를 도입해야 할까?", categoryId)))
                .andExpect(status().isCreated())
                .andReturn();
        return readLong(result, "$.id");
    }

    protected long createPublishedIssue(Cookie admin) throws Exception {
        long issueId = createDraftIssue(admin, createCategory(admin));
        mockMvc.perform(post("/api/admin/issues/{id}/review", issueId).cookie(admin)).andExpect(status().isOk());
        mockMvc.perform(post("/api/admin/issues/{id}/publish", issueId).cookie(admin)).andExpect(status().isOk());
        return issueId;
    }

    protected static String issueJson(String title, long categoryId) {
        return """
                {
                  "title": "%s",
                  "summary": "자체 작성한 중립적인 배경 설명",
                  "categoryId": %d,
                  "sources": [
                    {"url": "https://news.example.com/a/1", "publisher": "예시일보", "publishedOn": "2026-10-27"},
                    {"url": "https://news.example.com/b/2", "publisher": "샘플신문", "publishedOn": "2026-10-27"}
                  ]
                }
                """.formatted(title, categoryId);
    }

    protected static long readLong(MvcResult result, String path) throws Exception {
        Number value = JsonPath.read(result.getResponse().getContentAsString(), path);
        return value.longValue();
    }
}
