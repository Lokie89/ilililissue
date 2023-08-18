package com.ilililissue.issue.domain.entity;

import com.ilililissue.issue.domain.UnauthorizedException;
import com.ilililissue.member.domain.Member;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
public class Issue {

    private Member register;

    public Issue(Member register) {
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
}
