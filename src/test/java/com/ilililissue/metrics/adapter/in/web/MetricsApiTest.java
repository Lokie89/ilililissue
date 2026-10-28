package com.ilililissue.metrics.adapter.in.web;

import com.ilililissue.support.ApiTestSupport;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 다른 테스트도 같은 DB에 이벤트를 남기므로 행동 전후 지표의 차이로 검증한다.
 */
public class MetricsApiTest extends ApiTestSupport {

    private Map<String, Object> metrics(Cookie admin) throws Exception {
        String today = LocalDate.now(ZoneId.of("Asia/Seoul")).toString();
        String body = mockMvc.perform(get("/api/admin/metrics").cookie(admin)
                        .param("from", today).param("to", today))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$");
    }

    private static long diff(Map<String, Object> before, Map<String, Object> after, String key) {
        return ((Number) after.get(key)).longValue() - ((Number) before.get(key)).longValue();
    }

    @DisplayName("이슈 조회, 투표, 의견 작성, 다른 입장 탭 열람이 지표에 반영된다")
    @Test
    void recordsParticipation() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long issueId = createPublishedIssue(admin);
        Cookie member = loginAsNewUser();
        Map<String, Object> before = metrics(admin);

        // 상세 조회 API는 지표에 기록하지 않는다.
        mockMvc.perform(get("/api/issues/{id}", issueId)).andExpect(status().isOk());
        // 처음 온 방문자에게 쿠키를 발급하고, 같은 쿠키로 다시 보면 고유 방문자는 늘지 않는다.
        Cookie anonymous = mockMvc.perform(post("/api/issues/{id}/views", issueId))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists(IssueViewController.VISITOR_ID))
                .andReturn().getResponse().getCookie(IssueViewController.VISITOR_ID);
        mockMvc.perform(post("/api/issues/{id}/views", issueId).cookie(anonymous))
                .andExpect(status().isNoContent())
                .andExpect(cookie().doesNotExist(IssueViewController.VISITOR_ID));
        mockMvc.perform(post("/api/issues/{id}/views", issueId).cookie(member))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/issues/{id}/views", 999_999)).andExpect(status().isNotFound());
        // 앱은 직접 만든 방문자 ID를 헤더로 보낸다. 쿠키를 발급하지 않고, 같은 ID는 한 방문자로 센다.
        String appVisitor = java.util.UUID.randomUUID().toString();
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/issues/{id}/views", issueId).header(IssueViewController.VISITOR_HEADER, appVisitor))
                    .andExpect(status().isNoContent())
                    .andExpect(cookie().doesNotExist(IssueViewController.VISITOR_ID));
        }
        mockMvc.perform(post("/api/issues/{id}/votes", issueId).cookie(member)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"stance\":\"AGREE\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/issues/{id}/opinions", issueId).cookie(member)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"의견\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(member).param("stance", "AGREE"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(member).param("stance", "DISAGREE"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(member)
                        .param("stance", "NEUTRAL").param("page", "1"))
                .andExpect(status().isOk());

        Map<String, Object> after = metrics(admin);
        assertThat(diff(before, after, "issueViews")).isEqualTo(5);
        assertThat(diff(before, after, "issueVisitors")).isEqualTo(3);
        assertThat(diff(before, after, "votes")).isEqualTo(1);
        assertThat(diff(before, after, "voters")).isEqualTo(1);
        assertThat(diff(before, after, "opinions")).isEqualTo(1);
        assertThat(diff(before, after, "opposingViewers")).isEqualTo(1);
    }

    @DisplayName("일반 회원은 지표를 볼 수 없다")
    @Test
    void adminOnly() throws Exception {
        mockMvc.perform(get("/api/admin/metrics").cookie(loginAsNewUser())
                        .param("from", "2026-10-01").param("to", "2026-10-31"))
                .andExpect(status().isForbidden());
    }
}
