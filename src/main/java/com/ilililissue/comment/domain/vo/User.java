package com.ilililissue.comment.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
@EqualsAndHashCode(of = "id", callSuper = false)
public class User extends SelfValidating<User> {

    @NotNull
    private final Id id;
    
    @NotNull
    private final String username;

    private User(
            Id id,
            String username
    ) {
        this.id = id;
        this.username = username;
        this.validate();
    }

    public static User of(Id id, String username) {
        return new User(id, username);
    }

}
