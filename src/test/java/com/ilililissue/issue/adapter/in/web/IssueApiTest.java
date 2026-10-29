package com.ilililissue.issue.adapter.in.web;

import com.ilililissue.support.ApiTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IssueApiTest extends ApiTestSupport {

    @DisplayName("관리자가 작성·검수·게시한 이슈를 비회원이 조회한다")
    @Test
    void publishFlow() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long categoryId = createCategory(admin);
        long issueId = createDraftIssue(admin, categoryId);

        mockMvc.perform(get("/api/issues/{id}", issueId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/issues").param("size", "50"))
                .andExpect(jsonPath("$.content[*].id", not(hasItem((int) issueId))));

        mockMvc.perform(post("/api/admin/issues/{id}/publish", issueId).cookie(admin))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/admin/issues/{id}/review", issueId).cookie(admin))
                .andExpect(jsonPath("$.status").value("REVIEWED"));
        mockMvc.perform(post("/api/admin/issues/{id}/publish", issueId).cookie(admin))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedAt").exists());

        mockMvc.perform(get("/api/issues/{id}", issueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("주 4일제를 도입해야 할까?"))
                .andExpect(jsonPath("$.category.id").value(categoryId))
                .andExpect(jsonPath("$.sources.length()").value(2))
                .andExpect(jsonPath("$.sources[0].publisher").value("예시일보"))
                .andExpect(jsonPath("$.sources[0].publishedOn").value("2026-10-27"))
                .andExpect(jsonPath("$.corrected").value(false));
        mockMvc.perform(get("/api/issues").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem((int) issueId)));
        mockMvc.perform(get("/api/categories"))
                .andExpect(jsonPath("$[*].id", hasItem((int) categoryId)));
    }

    @DisplayName("핵심 쟁점과 입장별 근거를 함께 저장하고, 한쪽 근거만 있으면 거절한다")
    @Test
    void focus() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long categoryId = createCategory(admin);
        String body = """
                {"title": "주 4일제를 도입해야 할까?", "summary": "사실관계", "categoryId": %d,
                 "sources": [{"url": "https://news.example.com/f/1", "publisher": "예시일보", "publishedOn": "2026-10-27"}],
                 "keyPoint": "생산성과 휴식 중 무엇을 앞세울까", "agreeReason": "휴식이 늘면 생산성도 오른다", "disagreeReason": "%s"}
                """;

        long issueId = readLong(mockMvc.perform(post("/api/admin/issues").cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body.formatted(categoryId, "업종별 여건이 다르다")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.keyPoint").value("생산성과 휴식 중 무엇을 앞세울까"))
                .andExpect(jsonPath("$.disagreeReason").value("업종별 여건이 다르다"))
                .andReturn(), "$.id");
        mockMvc.perform(get("/api/admin/issues/{id}", issueId).cookie(admin))
                .andExpect(jsonPath("$.agreeReason").value("휴식이 늘면 생산성도 오른다"));

        mockMvc.perform(post("/api/admin/issues").cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body.formatted(categoryId, "")))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("분야(카테고리)로 공개 이슈 목록을 거른다")
    @Test
    void filterByCategory() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long published = createPublishedIssue(admin);
        long categoryId = jdbcTemplate.queryForObject("SELECT category_id FROM issue WHERE id = ?", Long.class, published);
        long otherPublished = createPublishedIssue(admin);
        createDraftIssue(admin, categoryId);

        mockMvc.perform(get("/api/issues").param("categoryId", String.valueOf(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(published))
                .andExpect(jsonPath("$.content[*].id", not(hasItem((int) otherPublished))));
    }

    @DisplayName("게시된 이슈는 수정할 수 없고 배경 설명만 정정한다")
    @Test
    void correction() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long issueId = createPublishedIssue(admin);

        mockMvc.perform(put("/api/admin/issues/{id}", issueId)
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(issueJson("바뀐 질문", createCategory(admin))))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/admin/issues/{id}/correction", issueId)
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"summary":"정정된 배경 설명"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/issues/{id}", issueId))
                .andExpect(jsonPath("$.summary").value("정정된 배경 설명"))
                .andExpect(jsonPath("$.corrected").value(true))
                .andExpect(jsonPath("$.correctedAt").exists());

        mockMvc.perform(post("/api/admin/issues/{id}/close", issueId).cookie(admin))
                .andExpect(jsonPath("$.status").value("CLOSED"));
        mockMvc.perform(get("/api/issues/{id}", issueId)).andExpect(status().isOk());
    }

    @DisplayName("잘못된 출처, 없는 카테고리, 중복 카테고리는 거절한다")
    @Test
    void invalidRequests() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long categoryId = createCategory(admin);

        mockMvc.perform(post("/api/admin/issues")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(issueJson("질문", categoryId).replace("https://news.example.com/a/1", "ftp://x")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/admin/issues")
                        .cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(issueJson("질문", 999_999)))
                .andExpect(status().isNotFound());

        String name = "중복" + unique();
        mockMvc.perform(post("/api/admin/categories").cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/admin/categories").cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isConflict());
    }

    @DisplayName("일반 회원과 비회원은 관리자 API를 쓸 수 없다")
    @Test
    void adminOnly() throws Exception {
        Cookie user = loginAsNewUser();

        mockMvc.perform(post("/api/admin/issues")
                        .cookie(user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(issueJson("질문", 1)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }
}
