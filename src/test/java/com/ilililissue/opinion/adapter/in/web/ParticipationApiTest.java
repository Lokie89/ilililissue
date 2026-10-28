package com.ilililissue.opinion.adapter.in.web;

import com.ilililissue.support.ApiTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ParticipationApiTest extends ApiTestSupport {

    private ResultActions vote(Cookie member, long issueId, String stance) throws Exception {
        return mockMvc.perform(post("/api/issues/{id}/votes", issueId)
                .cookie(member)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stance\":\"" + stance + "\"}"));
    }

    private ResultActions writeOpinion(Cookie member, long issueId, String content) throws Exception {
        return mockMvc.perform(post("/api/issues/{id}/opinions", issueId)
                .cookie(member)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"" + content + "\"}"));
    }

    private long voteAndWrite(Cookie member, long issueId, String stance, String content) throws Exception {
        vote(member, issueId, stance).andExpect(status().isCreated());
        MvcResult result = writeOpinion(member, issueId, content).andExpect(status().isCreated()).andReturn();
        return readLong(result, "$.id");
    }

    @DisplayName("투표는 회원당 1회이고 결과는 투표한 회원에게만 공개된다")
    @Test
    void voteAndResults() throws Exception {
        long issueId = createPublishedIssue(loginAsNewAdmin());
        Cookie voter = loginAsNewUser();
        Cookie nonVoter = loginAsNewUser();

        mockMvc.perform(post("/api/issues/{id}/votes", issueId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"stance\":\"AGREE\"}"))
                .andExpect(status().isUnauthorized());
        vote(voter, issueId, "AGREE")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stance").value("AGREE"));
        vote(voter, issueId, "DISAGREE").andExpect(status().isConflict());
        vote(nonVoter, issueId, "MAYBE").andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/issues/{id}/results", issueId).cookie(voter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.stances[0].stance").value("AGREE"))
                .andExpect(jsonPath("$.stances[0].count").value(1))
                .andExpect(jsonPath("$.stances[0].percentage").value(100.0))
                .andExpect(jsonPath("$.stances[2].count").value(0))
                .andExpect(jsonPath("$.notice").exists());
        mockMvc.perform(get("/api/issues/{id}/results", issueId).cookie(nonVoter))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/issues/{id}/results", issueId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/issues/{id}/participation", issueId))
                .andExpect(jsonPath("$.voted").value(false));
        mockMvc.perform(get("/api/issues/{id}/participation", issueId).cookie(voter))
                .andExpect(jsonPath("$.voted").value(true))
                .andExpect(jsonPath("$.stance").value("AGREE"))
                .andExpect(jsonPath("$.opinion").doesNotExist());
    }

    @DisplayName("게시 전 이슈와 마감된 이슈에는 투표할 수 없다")
    @Test
    void voteOnUnavailableIssue() throws Exception {
        Cookie admin = loginAsNewAdmin();
        Cookie user = loginAsNewUser();
        long draftId = createDraftIssue(admin, createCategory(admin));
        long closedId = createPublishedIssue(admin);
        mockMvc.perform(post("/api/admin/issues/{id}/close", closedId).cookie(admin)).andExpect(status().isOk());

        vote(user, draftId, "AGREE").andExpect(status().isNotFound());
        vote(user, closedId, "AGREE").andExpect(status().isConflict());
    }

    @DisplayName("의견은 투표 후 1개만 쓰고, 작성자만 1회 수정한다")
    @Test
    void writeAndEditOpinion() throws Exception {
        long issueId = createPublishedIssue(loginAsNewAdmin());
        Cookie author = loginAsNewUser();
        Cookie other = loginAsNewUser();

        writeOpinion(author, issueId, "투표 전 의견").andExpect(status().isForbidden());
        vote(author, issueId, "NEUTRAL").andExpect(status().isCreated());
        MvcResult written = writeOpinion(author, issueId, "상황에 따라 다릅니다.")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stance").value("NEUTRAL"))
                .andExpect(jsonPath("$.edited").value(false))
                .andReturn();
        long opinionId = readLong(written, "$.id");
        writeOpinion(author, issueId, "두 번째 의견").andExpect(status().isConflict());
        writeOpinion(author, issueId, "가".repeat(501)).andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/opinions/{id}", opinionId).cookie(other)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"남의 수정\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/opinions/{id}", opinionId).cookie(author)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"수정한 의견\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정한 의견"))
                .andExpect(jsonPath("$.edited").value(true));
        mockMvc.perform(put("/api/opinions/{id}", opinionId).cookie(author)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"또 수정\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/issues/{id}/participation", issueId).cookie(author))
                .andExpect(jsonPath("$.opinion.id").value(opinionId))
                .andExpect(jsonPath("$.opinion.content").value("수정한 의견"));
    }

    @DisplayName("입장별 의견을 추천순·최신순으로 보고, 의견당 1회 추천한다")
    @Test
    void listAndRecommend() throws Exception {
        long issueId = createPublishedIssue(loginAsNewAdmin());
        long first = voteAndWrite(loginAsNewUser(), issueId, "AGREE", "첫 번째 찬성");
        long second = voteAndWrite(loginAsNewUser(), issueId, "AGREE", "두 번째 찬성");
        Cookie viewer = loginAsNewUser();
        long third = voteAndWrite(viewer, issueId, "AGREE", "세 번째 찬성");
        long disagree = voteAndWrite(loginAsNewUser(), issueId, "DISAGREE", "반대");
        Cookie nonVoter = loginAsNewUser();

        mockMvc.perform(post("/api/opinions/{id}/recommendations", second).cookie(nonVoter))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/opinions/{id}/recommendations", second).cookie(viewer))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/opinions/{id}/recommendations", second).cookie(viewer))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(viewer).param("stance", "AGREE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[*].id", contains((int) second, (int) third, (int) first)))
                .andExpect(jsonPath("$.content[0].recommendationCount").value(1))
                .andExpect(jsonPath("$.content[0].recommendedByMe").value(true))
                .andExpect(jsonPath("$.content[0].authorNickname").exists())
                .andExpect(jsonPath("$.content[1].mine").value(true));

        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(viewer)
                        .param("stance", "AGREE").param("sort", "LATEST"))
                .andExpect(jsonPath("$.content[*].id", contains((int) third, (int) second, (int) first)));

        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(viewer).param("stance", "DISAGREE"))
                .andExpect(jsonPath("$.content[*].id", contains((int) disagree)));

        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(nonVoter).param("stance", "AGREE"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).param("stance", "AGREE"))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("숨김 처리된 의견은 목록에서 빠지고 추천할 수 없다")
    @Test
    void hiddenOpinion() throws Exception {
        long issueId = createPublishedIssue(loginAsNewAdmin());
        long hiddenId = voteAndWrite(loginAsNewUser(), issueId, "AGREE", "숨겨질 의견");
        Cookie viewer = loginAsNewUser();
        vote(viewer, issueId, "AGREE").andExpect(status().isCreated());
        jdbcTemplate.update("UPDATE opinion SET hidden = true WHERE id = ?", hiddenId);

        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(viewer).param("stance", "AGREE"))
                .andExpect(jsonPath("$.totalElements").value(0));
        mockMvc.perform(post("/api/opinions/{id}/recommendations", hiddenId).cookie(viewer))
                .andExpect(status().isNotFound());
    }
}
