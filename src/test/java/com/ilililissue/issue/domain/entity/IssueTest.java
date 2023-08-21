package com.ilililissue.issue.domain.entity;

import com.ilililissue.issue.domain.entity.dto.UpdateIssueDto;
import com.ilililissue.issue.domain.vo.Id;
import com.ilililissue.issue.domain.vo.User;
import com.ilililissue.issue.domain.vo.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
public class IssueTest {

    @DisplayName("이슈 생성 테스트")
    @Test
    void createIssue() {
        User member = User.of(UserRole.GUEST);
        final String testImage = "src/test/resources/R1280x0.jpeg";

        Assertions.assertThatThrownBy(() -> Issue.of(testImage, member));
    }

    @DisplayName("이슈 동일성 테스트")
    @Test
    void equals() {
        User originRegister = User.of(UserRole.ADMINISTRATOR);
        Id originId = new Id(5);
        final String testImage = "src/test/resources/R1280x0.jpeg";
        Issue originIssue = Issue.withId(originId, testImage, originRegister);

        Issue sameIdAndRegisterIssue = Issue.withId(originId, testImage, originRegister);
        Assertions.assertThat(originIssue).isEqualTo(sameIdAndRegisterIssue);

        Id sameValueId = new Id(5);

        Issue sameValueIdAndRegisterIssue = Issue.withId(sameValueId, testImage, originRegister);
        Assertions.assertThat(originIssue).isEqualTo(sameValueIdAndRegisterIssue);

        User sameRoleRegister = User.of(UserRole.ADMINISTRATOR);

        Issue sameValueIdAndSameRoleRegisterIssue = Issue.withId(sameValueId, testImage, sameRoleRegister);
        Assertions.assertThat(originIssue).isEqualTo(sameValueIdAndSameRoleRegisterIssue);

        Id differentValueId = new Id(6);
        Issue differentValueIdAndSameRegisterIssue = Issue.withId(differentValueId, testImage, originRegister);
        Assertions.assertThat(originIssue).isNotEqualTo(differentValueIdAndSameRegisterIssue);
    }

    @DisplayName("이슈 업데이트 테스트")
    @Test
    void update() {
        final String testImage = "src/test/resources/R1280x0.jpeg";
        final User register = User.of(UserRole.ADMINISTRATOR);

        final Issue originIssue = Issue.of(testImage, register);

        final String updateImage = "src/test/resources/9580d2de3a1b7861ec90534c83346e5f.jpg";
        final UpdateIssueDto updateIssueDto = UpdateIssueDto.from(updateImage, register);
        originIssue.update(updateIssueDto);
    }

}
