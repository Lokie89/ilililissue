package com.ilililissue.member.adapter.in.web;

import com.ilililissue.support.ApiTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MemberBlockApiTest extends ApiTestSupport {

    private void vote(Cookie member, long issueId, String stance) throws Exception {
        mockMvc.perform(post("/api/issues/{id}/votes", issueId).cookie(member)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"stance\":\"%s\"}".formatted(stance)))
                .andExpect(status().isCreated());
    }

    private long writeOpinion(Cookie member, long issueId, String content) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/issues/{id}/opinions", issueId).cookie(member)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"%s\"}".formatted(content)))
                .andExpect(status().isCreated())
                .andReturn();
        return readLong(result, "$.id");
    }

    @DisplayName("작성자를 차단하면 내 의견 목록에서만 빠지고, 차단을 해제하면 다시 보인다")
    @Test
    void blockAndUnblock() throws Exception {
        long issueId = createPublishedIssue(loginAsNewAdmin());
        Cookie author = loginAsNewUser();
        Cookie other = loginAsNewUser();
        Cookie viewer = loginAsNewUser();
        vote(author, issueId, "AGREE");
        vote(other, issueId, "AGREE");
        vote(viewer, issueId, "DISAGREE");
        long blockedOpinion = writeOpinion(author, issueId, "차단될 의견");
        writeOpinion(other, issueId, "남는 의견");

        mockMvc.perform(post("/api/opinions/{id}/author-block", blockedOpinion))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/opinions/{id}/author-block", blockedOpinion).cookie(viewer))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/opinions/{id}/author-block", blockedOpinion).cookie(viewer))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(viewer).param("stance", "AGREE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].content").value("남는 의견"));
        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(other).param("stance", "AGREE"))
                .andExpect(jsonPath("$.totalElements").value(2));

        MvcResult blocks = mockMvc.perform(get("/api/members/me/blocks").cookie(viewer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nickname").isString())
                .andExpect(jsonPath("$[0].blockedAt").isString())
                .andReturn();
        long blockId = readLong(blocks, "$[0].id");

        mockMvc.perform(delete("/api/members/me/blocks/{id}", blockId).cookie(other))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/members/me/blocks").cookie(viewer))
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete("/api/members/me/blocks/{id}", blockId).cookie(viewer))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/members/me/blocks").cookie(viewer))
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/issues/{id}/opinions", issueId).cookie(viewer).param("stance", "AGREE"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @DisplayName("자기 의견의 작성자(자기 자신)는 차단할 수 없다")
    @Test
    void cannotBlockSelf() throws Exception {
        long issueId = createPublishedIssue(loginAsNewAdmin());
        Cookie author = loginAsNewUser();
        vote(author, issueId, "NEUTRAL");
        long opinionId = writeOpinion(author, issueId, "내 의견");

        mockMvc.perform(post("/api/opinions/{id}/author-block", opinionId).cookie(author))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("자기 자신은 차단할 수 없습니다."));
        mockMvc.perform(post("/api/opinions/{id}/author-block", 999_999_999L).cookie(author))
                .andExpect(status().isNotFound());
    }
}
