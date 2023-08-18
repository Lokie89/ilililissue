package com.ilililissue.issue.domain.entity;

import com.ilililissue.common.SelfValidating;
import com.ilililissue.issue.domain.UnauthorizedException;
import com.ilililissue.issue.domain.vo.Id;
import com.ilililissue.issue.domain.vo.User;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * <h2>이슈 도메인</h2>
 * <li>
 * <ul>이슈는 관리자만 생성할 수 있다.</ul>
 * </li>
 *
 * @author SeongRok.Oh
 * @since 2023/08/18
 *
 */
@EqualsAndHashCode(of = "id", callSuper = false)
public class Issue extends SelfValidating<Issue> {

    private Id id;

    @NotNull
    private User register;

    private Issue(
            Id id,
            User register) {
        this.id = id;
        this.register = register;
        this.validate();
    }

    protected void validate() {
        super.validate();
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
