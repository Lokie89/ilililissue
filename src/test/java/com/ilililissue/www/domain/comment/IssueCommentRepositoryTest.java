package com.ilililissue.www.domain.comment;

import com.ilililissue.www.domain.issue.SimpleIssue;
import com.ilililissue.www.domain.issue.SimpleIssueRepository;
import com.ilililissue.www.domain.member.IssueMember;
import com.ilililissue.www.domain.member.IssueMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("댓글 저장소 테스트")
@Transactional
@SpringBootTest(properties = "application-test.properties")
public class IssueCommentRepositoryTest {
    @Autowired
    IssueCommentRepository repository;

    @Autowired
    IssueMemberRepository memberRepository;

    @Autowired
    SimpleIssueRepository issueRepository;

    @DisplayName("댓글 저장소에 저장")
    @Test
    void saveIssueCommentTest() {
        IssueMember member = memberRepository.save(IssueMember.builder().name("회원1").build());
        IssueMember manager = memberRepository.save(new IssueMember());
        SimpleIssue simpleIssue = issueRepository.save(SimpleIssue.builder().creator(manager).title("애플도 VR을?… “고성능·고가형 헤드셋 개발 중”").images(new String[]{"apple1", "vr1"}).description("페이스북 자회사 오큘러스의 VR 헤드셋 '오큘러스 퀘스트2'. 애플이 개발 중인 헤드셋도 이와 유사할 것으로 예측된다.").build());
        IssueComment issueComment = IssueComment.builder().author(member).issue(simpleIssue).comment("저건 얼마나 비쌀라나??").build();

        repository.save(issueComment);
        List<IssueComment> issueCommentList = repository.findAll();
        IssueComment savedIssueComment = issueCommentList.get(0);
        assertEquals("저건 얼마나 비쌀라나??", savedIssueComment.getComment());
        IssueMember commentMember = savedIssueComment.getAuthor();
        assertEquals("회원1", commentMember.getName());
        SimpleIssue commentedIssue = savedIssueComment.getIssue();
        assertEquals("애플도 VR을?… “고성능·고가형 헤드셋 개발 중”", commentedIssue.getTitle());
    }
}
