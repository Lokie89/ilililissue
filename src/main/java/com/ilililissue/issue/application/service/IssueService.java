package com.ilililissue.issue.application.service;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.issue.application.CategoryException;
import com.ilililissue.issue.application.IssueNotFoundException;
import com.ilililissue.issue.application.port.in.CloseExpiredIssuesUseCase;
import com.ilililissue.issue.application.port.in.IssueCommand;
import com.ilililissue.issue.application.port.in.IssueQuery;
import com.ilililissue.issue.application.port.in.IssueView;
import com.ilililissue.issue.application.port.in.ManageIssueUseCase;
import com.ilililissue.issue.application.port.out.CategoryPort;
import com.ilililissue.issue.application.port.out.IssueImageStore;
import com.ilililissue.issue.application.port.out.IssuePort;
import com.ilililissue.issue.domain.entity.Category;
import com.ilililissue.issue.domain.entity.Issue;
import com.ilililissue.issue.domain.vo.CategoryId;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.issue.domain.vo.IssueStatus;
import com.ilililissue.issue.domain.vo.IssueSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class IssueService implements IssueQuery, ManageIssueUseCase, CloseExpiredIssuesUseCase {

    private static final Set<IssueStatus> PUBLIC_STATUSES = EnumSet.of(IssueStatus.PUBLISHED, IssueStatus.CLOSED);

    private final IssuePort issuePort;
    private final CategoryPort categoryPort;
    private final IssueImageStore issueImageStore;
    private final Clock clock;

    @Transactional(readOnly = true)
    @Override
    public PageResult<IssueView> getPublicIssues(PageQuery pageQuery) {
        return toViews(issuePort.findByStatusIn(PUBLIC_STATUSES, pageQuery));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<IssueView> getPublicIssues(CategoryId categoryId, PageQuery pageQuery) {
        return toViews(issuePort.findByStatusInAndCategory(PUBLIC_STATUSES, categoryId, pageQuery));
    }

    @Transactional(readOnly = true)
    @Override
    public IssueView getPublicIssue(IssueId id) {
        Issue issue = loadIssue(id);
        if (!PUBLIC_STATUSES.contains(issue.getStatus())) {
            throw new IssueNotFoundException();
        }
        return toView(issue);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<IssueView> getAllIssues(PageQuery pageQuery) {
        return toViews(issuePort.findByStatusIn(EnumSet.allOf(IssueStatus.class), pageQuery));
    }

    @Transactional(readOnly = true)
    @Override
    public IssueView getIssue(IssueId id) {
        return toView(loadIssue(id));
    }

    @Transactional(readOnly = true)
    @Override
    public byte[] getPublicImage(IssueId id) {
        Issue issue = loadIssue(id);
        if (!PUBLIC_STATUSES.contains(issue.getStatus())) {
            throw new IssueNotFoundException();
        }
        return loadImage(issue);
    }

    @Transactional(readOnly = true)
    @Override
    public byte[] getImage(IssueId id) {
        return loadImage(loadIssue(id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Category> getCategories() {
        return categoryPort.findAll();
    }

    @Transactional
    @Override
    public Category createCategory(String name) {
        Category category = Category.of(name);
        if (categoryPort.existsByName(category.getName())) {
            throw CategoryException.duplicate();
        }
        return categoryPort.save(category);
    }

    @Transactional
    @Override
    public IssueId create(IssueCommand command) {
        requireCategory(command.categoryId());
        Issue issue = Issue.draft(command.title(), command.summary(), command.categoryId(), command.sources(), command.focus());
        return issuePort.save(issue).getId();
    }

    @Transactional
    @Override
    public void edit(IssueId id, IssueCommand command) {
        requireCategory(command.categoryId());
        update(id, issue -> issue.edit(command.title(), command.summary(), command.categoryId(), command.sources(),
                command.focus()));
    }

    @Transactional
    @Override
    public void review(IssueId id) {
        update(id, Issue::review);
    }

    @Transactional
    @Override
    public void publish(IssueId id) {
        update(id, issue -> issue.publish(now()));
    }

    @Transactional
    @Override
    public void close(IssueId id) {
        update(id, Issue::close);
    }

    @Transactional
    @Override
    public void extend(IssueId id) {
        update(id, issue -> issue.extend(now()));
    }

    @Transactional
    @Override
    public void attachImage(IssueId id, byte[] jpeg) {
        Issue issue = loadIssue(id);
        String previous = issue.replaceImage(issueImageStore.save(jpeg));
        issuePort.save(issue);
        if (previous != null) {
            issueImageStore.delete(previous);
        }
    }

    @Transactional
    @Override
    public void removeImage(IssueId id) {
        Issue issue = loadIssue(id);
        String previous = issue.replaceImage(null);
        issuePort.save(issue);
        if (previous != null) {
            issueImageStore.delete(previous);
        }
    }

    @Transactional
    @Override
    public void correct(IssueId id, IssueSummary correctedSummary) {
        update(id, issue -> issue.correct(correctedSummary, now()));
    }

    @Transactional
    @Override
    public int closePublishedBeforeToday() {
        List<Issue> issues = issuePort.findPublishedBefore(LocalDate.now(clock).atStartOfDay());
        issues.forEach(issue -> {
            issue.close();
            issuePort.save(issue);
        });
        return issues.size();
    }

    private void update(IssueId id, Consumer<Issue> change) {
        Issue issue = loadIssue(id);
        change.accept(issue);
        issuePort.save(issue);
    }

    private byte[] loadImage(Issue issue) {
        if (!issue.hasImage()) {
            throw new IssueNotFoundException();
        }
        return issueImageStore.load(issue.getImageKey()).orElseThrow(IssueNotFoundException::new);
    }

    private Issue loadIssue(IssueId id) {
        return issuePort.findById(id).orElseThrow(IssueNotFoundException::new);
    }

    private void requireCategory(CategoryId categoryId) {
        if (categoryPort.findById(categoryId).isEmpty()) {
            throw CategoryException.notFound();
        }
    }

    private IssueView toView(Issue issue) {
        return new IssueView(issue, categoryPort.findById(issue.getCategoryId()).orElse(null));
    }

    private PageResult<IssueView> toViews(PageResult<Issue> issues) {
        Map<CategoryId, Category> categories = categoryPort.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
        return issues.map(issue -> new IssueView(issue, categories.get(issue.getCategoryId())));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
