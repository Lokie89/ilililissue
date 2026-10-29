package com.ilililissue.issue.adapter.out.persistence;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.issue.application.port.out.CategoryPort;
import com.ilililissue.issue.application.port.out.IssuePort;
import com.ilililissue.issue.domain.entity.Category;
import com.ilililissue.issue.domain.entity.Issue;
import com.ilililissue.issue.domain.vo.CategoryId;
import com.ilililissue.issue.domain.vo.IssueFocus;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.issue.domain.vo.IssueSource;
import com.ilililissue.issue.domain.vo.IssueSources;
import com.ilililissue.issue.domain.vo.IssueStatus;
import com.ilililissue.issue.domain.vo.IssueSummary;
import com.ilililissue.issue.domain.vo.IssueTitle;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class IssuePersistenceAdapter implements IssuePort, CategoryPort {

    private static final Sort LATEST = Sort.by(Sort.Order.desc("publishedAt").nullsLast(), Sort.Order.desc("id"));

    private final IssueRepository issueRepository;
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Optional<Issue> findById(IssueId id) {
        return issueRepository.findById(id.getValue()).map(this::mapToDomain);
    }

    @Override
    public PageResult<Issue> findByStatusIn(Collection<IssueStatus> statuses, PageQuery pageQuery) {
        Page<IssueJpaEntity> page = issueRepository.findByStatusIn(
                statuses, PageRequest.of(pageQuery.page(), pageQuery.size(), LATEST));
        return toPageResult(page, pageQuery);
    }

    @Override
    public PageResult<Issue> findByStatusInAndCategory(Collection<IssueStatus> statuses, CategoryId categoryId,
                                                       PageQuery pageQuery) {
        Page<IssueJpaEntity> page = issueRepository.findByStatusInAndCategoryId(
                statuses, categoryId.getValue(), PageRequest.of(pageQuery.page(), pageQuery.size(), LATEST));
        return toPageResult(page, pageQuery);
    }

    private PageResult<Issue> toPageResult(Page<IssueJpaEntity> page, PageQuery pageQuery) {
        return new PageResult<>(page.getContent().stream().map(this::mapToDomain).toList(),
                pageQuery.page(), pageQuery.size(), page.getTotalElements());
    }

    @Override
    public List<Issue> findPublishedBefore(LocalDateTime cutoff) {
        return issueRepository.findOpenedBefore(IssueStatus.PUBLISHED, cutoff).stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public Issue save(Issue issue) {
        IssueJpaEntity saved = issueRepository.save(new IssueJpaEntity(
                issue.getId() == null ? null : issue.getId().getValue(),
                issue.getTitle().getValue(),
                issue.getSummary().getValue(),
                issue.getCategoryId().getValue(),
                new ArrayList<>(issue.getSources().getValues().stream()
                        .map(source -> new IssueSourceEmbeddable(source.getUrl(), source.getPublisher(),
                                source.getPublishedOn()))
                        .toList()),
                issue.getFocus().getKeyPoint(),
                issue.getFocus().getAgreeReason(),
                issue.getFocus().getDisagreeReason(),
                issue.getStatus(),
                issue.getPublishedAt(),
                issue.getCorrectedAt(),
                issue.getExtendedAt(),
                issue.getImageKey()
        ));
        return mapToDomain(saved);
    }

    @Override
    public List<Category> findAll() {
        return categoryJpaRepository.findAll(Sort.by("id")).stream().map(this::mapToDomain).toList();
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return categoryJpaRepository.findById(id.getValue()).map(this::mapToDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryJpaRepository.existsByName(name);
    }

    @Override
    public Category save(Category category) {
        return mapToDomain(categoryJpaRepository.save(new CategoryJpaEntity(
                category.getId() == null ? null : category.getId().getValue(),
                category.getName()
        )));
    }

    private Issue mapToDomain(IssueJpaEntity entity) {
        return Issue.withId(
                new IssueId(entity.getId()),
                IssueTitle.from(entity.getTitle()),
                IssueSummary.from(entity.getSummary()),
                new CategoryId(entity.getCategoryId()),
                IssueSources.from(entity.getSources().stream()
                        .map(source -> IssueSource.of(source.getUrl(), source.getPublisher(), source.getPublishedOn()))
                        .toList()),
                IssueFocus.of(entity.getKeyPoint(), entity.getAgreeReason(), entity.getDisagreeReason()),
                entity.getStatus(),
                entity.getPublishedAt(),
                entity.getCorrectedAt(),
                entity.getExtendedAt(),
                entity.getImageKey()
        );
    }

    private Category mapToDomain(CategoryJpaEntity entity) {
        return Category.withId(new CategoryId(entity.getId()), entity.getName());
    }
}
