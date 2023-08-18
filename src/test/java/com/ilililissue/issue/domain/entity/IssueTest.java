package com.ilililissue.issue.domain.entity;

import com.ilililissue.issue.domain.vo.Id;
import com.ilililissue.issue.domain.vo.User;
import com.ilililissue.issue.domain.vo.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
public class IssueTest {

    @Test
    void createIssue() {
        User member = User.of(UserRole.GUEST);

        Assertions.assertThatThrownBy(() -> Issue.of(member));
    }

    @Test
    void equals() {
        User originRegister = User.of(UserRole.ADMINISTRATOR);
        Id originId = new Id(5);
        Issue originIssue = Issue.withId(originId, originRegister);

        Issue sameIdAndRegisterIssue = Issue.withId(originId, originRegister);
        Assertions.assertThat(originIssue).isEqualTo(sameIdAndRegisterIssue);

        Id sameValueId = new Id(5);

        Issue sameValueIdAndRegisterIssue = Issue.withId(sameValueId, originRegister);
        Assertions.assertThat(originIssue).isEqualTo(sameValueIdAndRegisterIssue);

        User sameRoleRegister = User.of(UserRole.ADMINISTRATOR);

        Issue sameValueIdAndSameRoleRegisterIssue = Issue.withId(sameValueId, sameRoleRegister);
        Assertions.assertThat(originIssue).isEqualTo(sameValueIdAndSameRoleRegisterIssue);

        Id differentValueId = new Id(6);
        Issue differentValueIdAndSameRegisterIssue = Issue.withId(differentValueId, originRegister);
        Assertions.assertThat(originIssue).isNotEqualTo(differentValueIdAndSameRegisterIssue);

    }
}
