package com.ilililissue.auth.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class AuthApiTest {

    private static final String PASSWORD = "password1234";

    @Autowired
    MockMvc mockMvc;

    private String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private String uniqueNickname() {
        return "닉" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void signup(String email, String nickname) throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","nickname":"%s","password":"%s","agreedToTerms":true}
                                """.formatted(email, nickname, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());
    }

    private MvcResult login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andReturn();
    }

    @DisplayName("가입 → 로그인 → 내 정보 → 재발급 → 로그아웃")
    @Test
    void authFlow() throws Exception {
        String email = uniqueEmail();
        String nickname = uniqueNickname();
        signup(email, nickname);

        MvcResult login = login(email, PASSWORD);
        assertThat(login.getResponse().getStatus()).isEqualTo(200);
        Cookie accessToken = login.getResponse().getCookie(AuthCookies.ACCESS_TOKEN);
        Cookie refreshToken = login.getResponse().getCookie(AuthCookies.REFRESH_TOKEN);
        assertThat(accessToken.isHttpOnly()).isTrue();
        assertThat(refreshToken.isHttpOnly()).isTrue();
        assertThat(refreshToken.getPath()).isEqualTo(AuthCookies.REFRESH_PATH);
        assertThat(login.getResponse().getHeaders(HttpHeaders.SET_COOKIE)).allMatch(c -> c.contains("SameSite=Lax"));

        mockMvc.perform(get("/api/members/me").cookie(accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.nickname").value(nickname))
                .andExpect(jsonPath("$.role").value("USER"));

        mockMvc.perform(get("/api/members/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getValue()))
                .andExpect(status().isOk());

        MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh").cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(AuthCookies.ACCESS_TOKEN))
                .andReturn();
        Cookie rotatedRefreshToken = refreshed.getResponse().getCookie(AuthCookies.REFRESH_TOKEN);
        assertThat(rotatedRefreshToken.getValue()).isNotEqualTo(refreshToken.getValue());

        mockMvc.perform(post("/api/auth/refresh").cookie(refreshToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/logout").cookie(rotatedRefreshToken))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(AuthCookies.ACCESS_TOKEN, 0))
                .andExpect(cookie().maxAge(AuthCookies.REFRESH_TOKEN, 0));

        mockMvc.perform(post("/api/auth/refresh").cookie(rotatedRefreshToken))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("비밀번호가 틀리거나 없는 이메일이면 같은 401 응답을 준다")
    @Test
    void loginFailure() throws Exception {
        String email = uniqueEmail();
        signup(email, uniqueNickname());

        MvcResult wrongPassword = login(email, "wrong-password");
        MvcResult unknownEmail = login(uniqueEmail(), PASSWORD);

        assertThat(wrongPassword.getResponse().getStatus()).isEqualTo(401);
        assertThat(unknownEmail.getResponse().getStatus()).isEqualTo(401);
        assertThat(wrongPassword.getResponse().getContentAsString())
                .isEqualTo(unknownEmail.getResponse().getContentAsString());
        assertThat(wrongPassword.getResponse().getCookie(AuthCookies.ACCESS_TOKEN)).isNull();
    }

    @DisplayName("이메일(대소문자 무시)이나 닉네임이 중복되면 가입할 수 없다")
    @Test
    void duplicateSignup() throws Exception {
        String email = uniqueEmail();
        String nickname = uniqueNickname();
        signup(email, nickname);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","nickname":"%s","password":"%s","agreedToTerms":true}
                                """.formatted(email.toUpperCase(), uniqueNickname(), PASSWORD)))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","nickname":"%s","password":"%s","agreedToTerms":true}
                                """.formatted(uniqueEmail(), nickname, PASSWORD)))
                .andExpect(status().isConflict());
    }

    @DisplayName("짧은 비밀번호, 잘못된 이메일, 약관 미동의로는 가입할 수 없다")
    @Test
    void invalidSignup() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","nickname":"%s","password":"short","agreedToTerms":true}
                                """.formatted(uniqueEmail(), uniqueNickname())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-email","nickname":"%s","password":"%s","agreedToTerms":true}
                                """.formatted(uniqueNickname(), PASSWORD)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","nickname":"%s","password":"%s"}
                                """.formatted(uniqueEmail(), uniqueNickname(), PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("이용약관과 개인정보 처리방침에 동의해야 가입할 수 있습니다.")));
    }

    @DisplayName("비로그인은 401, 일반 회원의 관리자 경로 접근은 403")
    @Test
    void authorization() throws Exception {
        String email = uniqueEmail();
        signup(email, uniqueNickname());
        Cookie accessToken = login(email, PASSWORD).getResponse().getCookie(AuthCookies.ACCESS_TOKEN);

        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mockMvc.perform(get("/api/admin/issues").cookie(accessToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/members/me").cookie(new Cookie(AuthCookies.ACCESS_TOKEN, "forged.token.value")))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("앱은 토큰을 응답 본문으로 받아 헤더로 인증하고, 본문의 리프레시 토큰으로 재발급·로그아웃한다")
    @Test
    void appAuthFlow() throws Exception {
        String email = uniqueEmail();
        signup(email, uniqueNickname());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .header(AuthController.CLIENT_HEADER, AuthController.APP_CLIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(AuthCookies.ACCESS_TOKEN))
                .andExpect(cookie().doesNotExist(AuthCookies.REFRESH_TOKEN))
                .andExpect(jsonPath("$.member.email").value(email))
                .andExpect(jsonPath("$.accessTokenExpiresIn").value(1800))
                .andExpect(jsonPath("$.refreshTokenExpiresIn").value(14 * 24 * 3600))
                .andReturn();
        assertThat(login.getResponse().getHeader(HttpHeaders.CACHE_CONTROL)).contains("no-store");
        String accessToken = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
        String refreshToken = JsonPath.read(login.getResponse().getContentAsString(), "$.refreshToken");

        mockMvc.perform(get("/api/members/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh")
                        .header(AuthController.CLIENT_HEADER, AuthController.APP_CLIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();
        String rotated = JsonPath.read(refreshed.getResponse().getContentAsString(), "$.refreshToken");
        assertThat(rotated).isNotEqualTo(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .header(AuthController.CLIENT_HEADER, AuthController.APP_CLIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(refreshToken)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").header(AuthController.CLIENT_HEADER, AuthController.APP_CLIENT))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/logout")
                        .header(AuthController.CLIENT_HEADER, AuthController.APP_CLIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(rotated)))
                .andExpect(status().isNoContent())
                .andExpect(cookie().doesNotExist(AuthCookies.ACCESS_TOKEN));
        mockMvc.perform(post("/api/auth/refresh")
                        .header(AuthController.CLIENT_HEADER, AuthController.APP_CLIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(rotated)))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("잘못된 액세스 쿠키가 남아 있어도 로그인할 수 있다")
    @Test
    void loginWithStaleCookie() throws Exception {
        String email = uniqueEmail();
        signup(email, uniqueNickname());

        mockMvc.perform(post("/api/auth/login")
                        .cookie(new Cookie(AuthCookies.ACCESS_TOKEN, "expired.token.value"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk());
    }
}
