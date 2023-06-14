package com.ilililissue.comment.adapter.persistence;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class CommentJpaRepositoryTest {

    @Autowired
    CommentJpaRepository commentJpaRepository;

    @DisplayName("댓글 저장 테스트")
    @Test
    void save() {
        final String comment = "댓글1";
        final Long userId = 3L;
        final Long issueId = 53L;
        final boolean updated = false;
        CommentJpaEntity savingCommentJpaEntity = CommentJpaEntity.withoutId(
                comment,
                userId,
                issueId,
                updated
        );

        commentJpaRepository.save(savingCommentJpaEntity);

        Optional<CommentJpaEntity> retrievedComment = commentJpaRepository.findById(savingCommentJpaEntity.getId());

        Assertions.assertThat(retrievedComment.isPresent()).isTrue();
    }

    @DisplayName("중복 댓글 저장 테스트")
    @Test
    void alreadySavedException() {
        final String comment = "댓글1";
        final Long userId = 3L;
        final Long issueId = 53L;
        final boolean updated = false;
        CommentJpaEntity savingCommentJpaEntity1 = CommentJpaEntity.withoutId(
                comment,
                userId,
                issueId,
                updated
        );

        CommentJpaEntity savingCommentJpaEntity2 = CommentJpaEntity.withoutId(
                comment,
                userId,
                issueId,
                updated
        );

        commentJpaRepository.saveAndFlush(savingCommentJpaEntity1);
        Assertions.assertThatThrownBy(() -> commentJpaRepository.saveAndFlush(savingCommentJpaEntity2));
    }
}
