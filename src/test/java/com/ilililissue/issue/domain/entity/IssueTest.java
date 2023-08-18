package com.ilililissue.issue.domain.entity;

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

        Assertions.assertThatThrownBy(() -> new Issue(member));
    }
}
