package com.ilililissue.issue.domain.vo;

import com.ilililissue.common.SelfValidating;
import lombok.EqualsAndHashCode;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
@EqualsAndHashCode(callSuper = false)
public class User extends SelfValidating<User> {

    private final UserRole role;

    private User(
            UserRole role
    ) {
        this.role = role;
        this.validate();
    }

    public static User of(UserRole role) {
        return new User(role);
    }

    public boolean isAdministrator() {
        return role == UserRole.ADMINISTRATOR;
    }
}
