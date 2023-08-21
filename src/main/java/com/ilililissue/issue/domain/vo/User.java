package com.ilililissue.issue.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
@EqualsAndHashCode(callSuper = false)
public class User extends SelfValidating<User> {

    @NotNull
    private final Id id;

    private final UserRole role;

    private User(
            Id id,
            UserRole role
    ) {
        this.id = id;
        this.role = role;
        this.validate();
    }

    public static User of(Id id, UserRole role) {
        return new User(id, role);
    }

    public boolean isAdministrator() {
        return role == UserRole.ADMINISTRATOR;
    }
}
