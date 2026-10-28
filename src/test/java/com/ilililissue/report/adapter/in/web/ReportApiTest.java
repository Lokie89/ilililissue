package com.ilililissue.report.adapter.in.web;

import com.ilililissue.support.ApiTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReportApiTest extends ApiTestSupport {

    private long writeOpinion(long issueId) throws Exception {
        Cookie author = loginAsNewUser();
        mockMvc.perform(post("/api/issues/{id}/votes", issueId).cookie(author)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"stance\":\"AGREE\"}"))
                .andExpect(status().isCreated());
        MvcResult result = mockMvc.perform(post("/api/issues/{id}/opinions", issueId).cookie(author)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"신고될 의견\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return readLong(result, "$.id");
    }

    private ResultActions report(Cookie reporter, long opinionId) throws Exception {
        return mockMvc.perform(post("/api/opinions/{id}/reports", opinionId).cookie(reporter)
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"욕설\"}"));
    }

    private Cookie voter(long issueId) throws Exception {
        Cookie member = loginAsNewUser();
        mockMvc.perform(post("/api/issues/{id}/votes", issueId).cookie(member)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"stance\":\"AGREE\"}"))
                .andExpect(status().isCreated());
        return member;
    }

    @DisplayName("신고를 숨김 처리하면 의견이 목록에서 빠지고 같은 의견의 다른 신고도 종결된다")
    @Test
    void hideByReport() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long issueId = createPublishedIssue(admin);
        long opinionId = writeOpinion(issueId);
        Cookie reporter = voter(issueId);
        Cookie otherReporter = loginAsNewUser();

        MvcResult reported = report(reporter, opinionId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andReturn();
        long reportId = readLong(reported, "$.id");
        report(reporter, opinionId).andExpect(status().isConflict());
        long otherReportId = readLong(report(otherReporter, opinionId).andReturn(), "$.id");

        mockMvc.perform(get("/api/admin/reports").cookie(admin).param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == %d)].opinionContent".formatted(reportId))
                        .value(hasItem("신고될 의견")));

        mockMvc.perform(post("/api/admin/reports/{id}/hide", reportId).cookie(admin))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/admin/reports/{id}/dismiss", otherReportId).cookie(admin))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/admin/reports").cookie(admin).param("status", "RESOLVED_HIDDEN").param("size", "50"))
                .andExpect(jsonPath("$.content[*].id", hasItem((int) reportId)))
                .andExpect(jsonPath("$.content[*].id", hasItem((int) otherReportId)));
        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(reporter).param("stance", "AGREE"))
                .andExpect(jsonPath("$.content[*].id", not(hasItem((int) opinionId))));
        report(loginAsNewUser(), opinionId).andExpect(status().isNotFound());
    }

    @DisplayName("기각된 신고는 의견을 그대로 둔다")
    @Test
    void dismiss() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long issueId = createPublishedIssue(admin);
        long opinionId = writeOpinion(issueId);
        Cookie reporter = voter(issueId);
        long reportId = readLong(report(reporter, opinionId).andReturn(), "$.id");

        mockMvc.perform(post("/api/admin/reports/{id}/dismiss", reportId).cookie(admin))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/admin/reports/{id}/hide", reportId).cookie(admin))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(reporter).param("stance", "AGREE"))
                .andExpect(jsonPath("$.content[*].id", hasItem((int) opinionId)));
    }

    @DisplayName("관리자는 신고 없이 의견을 숨길 수 있고, 일반 회원은 신고 관리 API를 쓸 수 없다")
    @Test
    void adminHideAndAuthorization() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long issueId = createPublishedIssue(admin);
        long opinionId = writeOpinion(issueId);
        Cookie member = voter(issueId);

        mockMvc.perform(get("/api/admin/reports").cookie(member)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/opinions/{id}/hide", opinionId).cookie(member))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/opinions/{id}/hide", opinionId).cookie(admin))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(member).param("stance", "AGREE"))
                .andExpect(jsonPath("$.content[*].id", not(hasItem((int) opinionId))));
        report(member, opinionId).andExpect(status().isNotFound());
        report(member, 999_999).andExpect(status().isNotFound());
    }

    @DisplayName("관리자는 숨긴 의견 목록을 보고 숨김을 되돌릴 수 있다")
    @Test
    void unhide() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long issueId = createPublishedIssue(admin);
        long opinionId = writeOpinion(issueId);
        Cookie member = voter(issueId);
        mockMvc.perform(post("/api/admin/opinions/{id}/hide", opinionId).cookie(admin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/opinions/hidden").cookie(admin).param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem((int) opinionId)));
        mockMvc.perform(post("/api/admin/opinions/{id}/unhide", opinionId).cookie(member))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/opinions/{id}/unhide", opinionId).cookie(admin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/opinions/hidden").cookie(admin).param("size", "100"))
                .andExpect(jsonPath("$.content[*].id", not(hasItem((int) opinionId))));
        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(member).param("stance", "AGREE"))
                .andExpect(jsonPath("$.content[*].id", hasItem((int) opinionId)));
    }
}
