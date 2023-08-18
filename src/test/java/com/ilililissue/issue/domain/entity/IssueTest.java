package com.ilililissue.issue.domain.entity;

import com.ilililissue.issue.domain.vo.Id;
import com.ilililissue.member.domain.Member;
import com.ilililissue.member.domain.MemberRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
public class IssueTest {

    @Test
    void createIssue() {
        Member member = new Member(MemberRole.GUEST);

        Assertions.assertThatThrownBy(() -> new Issue(null, member));
    }

    @Test
    void equals() {
        Member originRegister = new Member(MemberRole.ADMINISTRATOR);
        Id originId = new Id(5);
        Issue originIssue = new Issue(originId, originRegister);

        Issue sameIdAndRegisterIssue = new Issue(originId, originRegister);
        Assertions.assertThat(originIssue).isEqualTo(sameIdAndRegisterIssue);

        Id sameValueId = new Id(5);

        Issue sameValueIdAndRegisterIssue = new Issue(sameValueId, originRegister);
        Assertions.assertThat(originIssue).isEqualTo(sameValueIdAndRegisterIssue);

        Member sameRoleRegister = new Member(MemberRole.ADMINISTRATOR);

        Issue sameValueIdAndSameRoleRegisterIssue = new Issue(sameValueId, sameRoleRegister);
        Assertions.assertThat(originIssue).isEqualTo(sameValueIdAndSameRoleRegisterIssue);

        Id differentValueId = new Id(6);
        Issue differentValueIdAndSameRegisterIssue = new Issue(differentValueId, originRegister);
        Assertions.assertThat(originIssue).isNotEqualTo(differentValueIdAndSameRegisterIssue);

    }
}
