package com.ilililissue.issue.domain.entity;

import com.ilililissue.issue.domain.entity.dto.UpdateIssueDto;
import com.ilililissue.issue.domain.vo.*;
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
        final String prefixImageFileName = "src/test/resources/";
        final String testImage = "R1280x0";
        final ImageExtension extension = ImageExtension.from("jpeg");
        final IssueImages issueImages = IssueImages.from(IssueImage.of(prefixImageFileName + testImage, extension));

        Assertions.assertThatThrownBy(() -> Issue.of(issueImages, member));
    }

    @DisplayName("이슈 동일성 테스트")
    @Test
    void equals() {
        User originRegister = User.of(UserRole.ADMINISTRATOR);
        Id originId = new Id(5);

        final String prefixImageFileName = "src/test/resources/";
        final String testImage = "R1280x0";
        final ImageExtension extension = ImageExtension.from("jpeg");
        final IssueImages issueImages = IssueImages.from(IssueImage.of(prefixImageFileName + testImage, extension));
        Issue originIssue = Issue.withId(originId, issueImages, originRegister);

        Issue sameIdAndRegisterIssue = Issue.withId(originId, issueImages, originRegister);
        Assertions.assertThat(originIssue).isEqualTo(sameIdAndRegisterIssue);

        Id sameValueId = new Id(5);

        Issue sameValueIdAndRegisterIssue = Issue.withId(sameValueId, issueImages, originRegister);
        Assertions.assertThat(originIssue).isEqualTo(sameValueIdAndRegisterIssue);

        User sameRoleRegister = User.of(UserRole.ADMINISTRATOR);

        Issue sameValueIdAndSameRoleRegisterIssue = Issue.withId(sameValueId, issueImages, sameRoleRegister);
        Assertions.assertThat(originIssue).isEqualTo(sameValueIdAndSameRoleRegisterIssue);

        Id differentValueId = new Id(6);
        Issue differentValueIdAndSameRegisterIssue = Issue.withId(differentValueId, issueImages, originRegister);
        Assertions.assertThat(originIssue).isNotEqualTo(differentValueIdAndSameRegisterIssue);
    }

    @DisplayName("이슈 업데이트 테스트")
    @Test
    void update() {
        final String prefixImageFileName = "src/test/resources/";
        final String testImage = "R1280x0";
        final ImageExtension extension = ImageExtension.from("jpeg");
        final IssueImages issueImages = IssueImages.from(IssueImage.of(prefixImageFileName + testImage, extension));
        final User register = User.of(UserRole.ADMINISTRATOR);

        final Issue originIssue = Issue.of(issueImages, register);


        final String updateImage = "9580d2de3a1b7861ec90534c83346e5f";
        final ImageExtension updateImageExtension = ImageExtension.from("jpg");
        final IssueImages updateIssueImages = IssueImages.from(IssueImage.of(prefixImageFileName + updateImage, updateImageExtension));
        final UpdateIssueDto updateIssueDto = UpdateIssueDto.from(updateIssueImages, register);
        originIssue.update(updateIssueDto);

        final User notRegister = User.of(UserRole.GUEST);
        final UpdateIssueDto notRegisterUpdateIssueDto = UpdateIssueDto.from(updateIssueImages, notRegister);
        Assertions.assertThatThrownBy(() -> originIssue.update(notRegisterUpdateIssueDto));

    }

}
