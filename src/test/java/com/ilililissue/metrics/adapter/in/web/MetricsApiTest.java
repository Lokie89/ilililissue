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

        mockMvc.perform(get("/api/issues/{id}", issueId)).andExpect(status().isOk());
        mockMvc.perform(get("/api/issues/{id}", issueId).cookie(member)).andExpect(status().isOk());
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
        assertThat(diff(before, after, "issueViews")).isEqualTo(2);
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
