package com.ilililissue;

import com.ilililissue.issue.application.port.in.IssueCommand;
import com.ilililissue.issue.application.port.in.ManageIssueUseCase;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.application.port.in.RegisterMemberCommand;
import com.ilililissue.member.application.port.in.RegisterMemberUseCase;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;

/**
 * MySQL 없이 H2로 서버를 띄우는 로컬 개발용 진입점: {@code ./gradlew bootTestRun}
 * <p>
 * 테스트 클래스패스와 {@code src/test/resources/application.yml}을 쓰며, 시작할 때 아래 데이터를 만든다.
 * 운영 코드에는 포함되지 않는다.
 * <ul>
 * <li>관리자: admin@local.test / password1234</li>
 * <li>회원: user@local.test / password1234</li>
 * <li>카테고리 1개, 게시된 이슈 1개</li>
 * </ul>
 */
public class TestIlililissueApplication {

    public static void main(String[] args) {
        SpringApplication.from(IlililissueApplication::main)
                .with(DevSeed.class)
                .run(args);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class DevSeed {

        @Bean
        ApplicationRunner seed(RegisterMemberUseCase registerMemberUseCase, ManageIssueUseCase manageIssueUseCase,
                               JdbcTemplate jdbcTemplate) {
            return args -> {
                registerMemberUseCase.register(
                        new RegisterMemberCommand("admin@local.test", "관리자", "password1234"));
                jdbcTemplate.update("UPDATE member SET role = 'ADMIN' WHERE email = ?", "admin@local.test");
                registerMemberUseCase.register(
                        new RegisterMemberCommand("user@local.test", "토론러", "password1234"));

                long categoryId = manageIssueUseCase.createCategory("사회").getId().getValue();
                IssueId issueId = manageIssueUseCase.create(IssueCommand.of(
                        "주 4일제를 도입해야 할까?",
                        "일부 기업과 지자체가 주 4일제를 시범 운영하고 있다. 생산성과 삶의 질이 나아졌다는 평가와 "
                                + "업종별 형평성, 인건비 부담을 우려하는 의견이 함께 나온다. (개발용 예시 데이터)",
                        categoryId,
                        List.of(new IssueCommand.Source("https://news.example.com/four-day-week", "예시일보",
                                LocalDate.now()))
                ));
                manageIssueUseCase.review(issueId);
                manageIssueUseCase.publish(issueId);
            };
        }
    }
}
