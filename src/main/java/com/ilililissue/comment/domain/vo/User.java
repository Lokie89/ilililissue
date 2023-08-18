package com.ilililissue.comment.domain.vo;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
@EqualsAndHashCode(of = "id")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    private final Id id;
    private final String username;

    public static User of(Id id, String username) {
        return new User(id, username);
    }
}
