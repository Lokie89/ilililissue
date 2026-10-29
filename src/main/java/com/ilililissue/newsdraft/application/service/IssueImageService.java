package com.ilililissue.newsdraft.application.service;

import com.ilililissue.issue.application.port.in.IssueQuery;
import com.ilililissue.issue.application.port.in.IssueView;
import com.ilililissue.issue.application.port.in.ManageIssueUseCase;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.newsdraft.application.port.in.GenerateIssueImageUseCase;
import com.ilililissue.newsdraft.application.port.out.IssueImageGeneratorPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 이슈 대표 이미지 만들기. 외부 호출이 길어 트랜잭션 밖에서 만들고, 붙이는 것만 이슈 모듈에 맡긴다.
 * 같은 이슈를 동시에 두 번 만들지 않도록 메모리에서 막는다(서버 한 대 전제).
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class IssueImageService implements GenerateIssueImageUseCase {

    private final IssueImageGeneratorPort issueImageGeneratorPort;
    private final IssueQuery issueQuery;
    private final ManageIssueUseCase manageIssueUseCase;

    private final Set<Long> generating = ConcurrentHashMap.newKeySet();

    @Override
    public boolean available() {
        return issueImageGeneratorPort.available();
    }

    @Override
    public boolean generate(IssueId issueId) {
        if (!available() || !generating.add(issueId.getValue())) {
            return false;
        }
        try {
            IssueView view = issueQuery.getIssue(issueId);
            Optional<byte[]> image = issueImageGeneratorPort.generate(
                    view.issue().getTitle().getValue(),
                    view.issue().getSummary().getValue(),
                    view.category() == null ? null : view.category().getName());
            if (image.isEmpty()) {
                return false;
            }
            manageIssueUseCase.attachImage(issueId, image.get());
            log.info("이슈 #{} 대표 이미지를 만들었습니다({}KB).", issueId.getValue(), image.get().length / 1024);
            return true;
        } catch (RuntimeException e) {
            log.warn("이슈 #{} 대표 이미지를 만들지 못했습니다: {}", issueId.getValue(), e.getMessage());
            return false;
        } finally {
            generating.remove(issueId.getValue());
        }
    }

    @Override
    public boolean isGenerating(IssueId issueId) {
        return generating.contains(issueId.getValue());
    }
}
