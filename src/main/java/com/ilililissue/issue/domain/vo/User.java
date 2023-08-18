package com.ilililissue.issue.domain.vo;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    private final UserRole role;

    public static User of(UserRole role) {
        return new User(role);
    }

    public boolean isAdministrator() {
        return role == UserRole.ADMINISTRATOR;
    }
}
