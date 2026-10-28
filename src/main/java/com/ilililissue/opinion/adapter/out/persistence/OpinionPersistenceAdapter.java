package com.ilililissue.opinion.adapter.out.persistence;

import com.ilililissue.common.application.PageQuery;
import com.ilililissue.common.application.PageResult;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.opinion.application.port.in.OpinionSort;
import com.ilililissue.opinion.application.port.out.OpinionPort;
import com.ilililissue.opinion.application.port.out.RecommendationPort;
import com.ilililissue.opinion.domain.entity.Opinion;
import com.ilililissue.opinion.domain.entity.Recommendation;
import com.ilililissue.opinion.domain.vo.OpinionContent;
import com.ilililissue.opinion.domain.vo.OpinionId;
import com.ilililissue.vote.domain.vo.Stance;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class OpinionPersistenceAdapter implements OpinionPort, RecommendationPort {

    private static final Sort RECOMMENDED = Sort.by(Sort.Order.desc("recommendationCount"), Sort.Order.desc("id"));
    private static final Sort LATEST = Sort.by(Sort.Order.desc("writtenAt"), Sort.Order.desc("id"));

    private final OpinionJpaRepository opinionJpaRepository;
    private final RecommendationJpaRepository recommendationJpaRepository;

    @Override
    public Opinion save(Opinion opinion) {
        if (opinion.getId() == null) {
            return mapToDomain(opinionJpaRepository.save(new OpinionJpaEntity(
                    null,
                    opinion.getIssueId().getValue(),
                    opinion.getAuthorId().getValue(),
                    opinion.getStance(),
                    opinion.getContent().getValue(),
                    opinion.isEdited(),
                    opinion.isHidden(),
                    opinion.isDeleted(),
                    0,
                    opinion.getWrittenAt()
            )));
        }
        // 추천 수는 별도 갱신하므로 수정 시 내용·상태만 반영한다.
        OpinionJpaEntity entity = opinionJpaRepository.findById(opinion.getId().getValue()).orElseThrow();
        entity.update(opinion.getContent().getValue(), opinion.isEdited(), opinion.isHidden(), opinion.isDeleted());
        return mapToDomain(entity);
    }

    @Override
    public Optional<Opinion> findById(OpinionId id) {
        return opinionJpaRepository.findById(id.getValue()).map(this::mapToDomain);
    }

    @Override
    public Optional<Opinion> findByIssueIdAndAuthorId(IssueId issueId, MemberId authorId) {
        return opinionJpaRepository.findByIssueIdAndAuthorId(issueId.getValue(), authorId.getValue())
                .map(this::mapToDomain);
    }

    @Override
    public PageResult<CountedOpinion> findVisible(IssueId issueId, Stance stance, OpinionSort sort,
                                                  PageQuery pageQuery) {
        Sort order = sort == OpinionSort.LATEST ? LATEST : RECOMMENDED;
        Page<OpinionJpaEntity> page = opinionJpaRepository.findByIssueIdAndStanceAndHiddenFalseAndDeletedFalse(
                issueId.getValue(), stance, PageRequest.of(pageQuery.page(), pageQuery.size(), order));
        return new PageResult<>(
                page.getContent().stream()
                        .map(entity -> new CountedOpinion(mapToDomain(entity), entity.getRecommendationCount()))
                        .toList(),
                pageQuery.page(), pageQuery.size(), page.getTotalElements());
    }

    @Override
    public void incrementRecommendationCount(OpinionId id) {
        opinionJpaRepository.incrementRecommendationCount(id.getValue());
    }

    @Override
    public void decrementRecommendationCount(OpinionId id) {
        opinionJpaRepository.decrementRecommendationCount(id.getValue());
    }

    @Override
    public void save(Recommendation recommendation) {
        recommendationJpaRepository.save(new RecommendationJpaEntity(
                null,
                recommendation.getOpinionId().getValue(),
                recommendation.getMemberId().getValue(),
                recommendation.getRecommendedAt()
        ));
    }

    @Override
    public boolean exists(OpinionId opinionId, MemberId memberId) {
        return recommendationJpaRepository.existsByOpinionIdAndMemberId(opinionId.getValue(), memberId.getValue());
    }

    @Override
    public boolean delete(OpinionId opinionId, MemberId memberId) {
        return recommendationJpaRepository.deleteByOpinionIdAndMemberId(opinionId.getValue(), memberId.getValue()) > 0;
    }

    @Override
    public Set<OpinionId> findRecommendedOpinionIds(MemberId memberId, Collection<OpinionId> opinionIds) {
        if (opinionIds.isEmpty()) {
            return Set.of();
        }
        return recommendationJpaRepository.findRecommendedOpinionIds(
                        memberId.getValue(), opinionIds.stream().map(OpinionId::getValue).toList())
                .stream()
                .map(OpinionId::new)
                .collect(Collectors.toSet());
    }

    private Opinion mapToDomain(OpinionJpaEntity entity) {
        return Opinion.withId(
                new OpinionId(entity.getId()),
                new IssueId(entity.getIssueId()),
                new MemberId(entity.getAuthorId()),
                entity.getStance(),
                OpinionContent.from(entity.getContent()),
                entity.isEdited(),
                entity.isHidden(),
                entity.isDeleted(),
                entity.getWrittenAt()
        );
    }
}
