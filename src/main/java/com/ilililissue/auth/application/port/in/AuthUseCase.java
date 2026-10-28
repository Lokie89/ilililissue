package com.ilililissue.auth.application.port.in;

public interface AuthUseCase {

    AuthTokens login(String email, String rawPassword);

    AuthTokens refresh(String rawRefreshToken);

    void logout(String rawRefreshToken);
}
