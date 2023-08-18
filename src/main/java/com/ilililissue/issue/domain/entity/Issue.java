package com.ilililissue.issue.domain.entity;

import com.ilililissue.issue.domain.UnauthorizedException;
import com.ilililissue.issue.domain.vo.Id;
import com.ilililissue.issue.domain.vo.User;
import lombok.EqualsAndHashCode;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
@EqualsAndHashCode(of = "id")
public class Issue {
    private Id id;
    private User register;

    private Issue(
            Id id,
            User register) {
        this.id = id;
        this.register = register;
        this.validate();
    }

    private void validate() {
        this.validateRegister();
    }

    private void validateRegister() {
        if (!this.isAuthenticatedRegister()) {
            throw new UnauthorizedException();
        }
    }

    private boolean isAuthenticatedRegister() {
        return this.register.isAdministrator();
    }

    public static Issue withId(Id id, User register) {
        return new Issue(id, register);
    }

    public static Issue of(User register) {
        return new Issue(null, register);
    }
}
