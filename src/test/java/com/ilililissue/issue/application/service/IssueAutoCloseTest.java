package com.ilililissue.issue.application.service;

import com.ilililissue.issue.application.port.in.CloseExpiredIssuesUseCase;
import com.ilililissue.support.ApiTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IssueAutoCloseTest extends ApiTestSupport {

    private static final LocalDateTime TODAY = LocalDate.now(ZoneId.of("Asia/Seoul")).atStartOfDay();

    @Autowired
    CloseExpiredIssuesUseCase closeExpiredIssuesUseCase;

    private void setOpenedAt(long issueId, LocalDateTime publishedAt, LocalDateTime extendedAt) {
        jdbcTemplate.update("UPDATE issue SET published_at = ?, extended_at = ? WHERE id = ?", publishedAt, extendedAt, issueId);
    }

    private String statusOf(long issueId) {
        return jdbcTemplate.queryForObject("SELECT status FROM issue WHERE id = ?", String.class, issueId);
    }

    @DisplayName("오늘 0시 전에 게시(연장)된 게시 중 이슈만 마감한다")
    @Test
    void closesPreviousDays() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long yesterday = createPublishedIssue(admin);
        long today = createPublishedIssue(admin);
        long extendedToday = createPublishedIssue(admin);
        setOpenedAt(yesterday, TODAY.minusHours(16), null);
        setOpenedAt(today, TODAY.plusMinutes(30), null);
        setOpenedAt(extendedToday, TODAY.minusDays(2), TODAY.plusMinutes(10));

        int closed = closeExpiredIssuesUseCase.closePublishedBeforeToday();

        assertThat(closed).isGreaterThanOrEqualTo(1);
        assertThat(statusOf(yesterday)).isEqualTo("CLOSED");
        assertThat(statusOf(today)).isEqualTo("PUBLISHED");
        assertThat(statusOf(extendedToday)).isEqualTo("PUBLISHED");
    }

    @DisplayName("관리자가 마감된 이슈를 연장하면 다시 열리고, 다음 날 자동 마감에 다시 마감된다")
    @Test
    void extendReopensUntilNextMorning() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long issueId = createPublishedIssue(admin);
        mockMvc.perform(post("/api/admin/issues/{id}/close", issueId).cookie(admin)).andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/issues/{id}/extend", issueId).cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.extendedAt").isNotEmpty());
        closeExpiredIssuesUseCase.closePublishedBeforeToday();
        assertThat(statusOf(issueId)).isEqualTo("PUBLISHED");

        // 하루가 지나 연장 시각이 어제가 되면 다시 마감된다.
        setOpenedAt(issueId, TODAY.minusDays(3), TODAY.minusHours(5));
        closeExpiredIssuesUseCase.closePublishedBeforeToday();
        assertThat(statusOf(issueId)).isEqualTo("CLOSED");
    }

    @DisplayName("연장은 관리자만, 게시 전 이슈는 연장할 수 없다")
    @Test
    void extendRules() throws Exception {
        Cookie admin = loginAsNewAdmin();
        long published = createPublishedIssue(admin);
        long draft = createDraftIssue(admin, createCategory(admin));

        mockMvc.perform(post("/api/admin/issues/{id}/extend", published).cookie(loginAsNewUser()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/issues/{id}/extend", draft).cookie(admin))
                .andExpect(status().isConflict());
    }
}
