package com.ilililissue.member.adapter.out.persistence;

import com.ilililissue.member.domain.entity.Member;
import com.ilililissue.member.domain.vo.Email;
import com.ilililissue.member.domain.vo.Nickname;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

@Import(MemberPersistenceAdapter.class)
@DataJpaTest
public class MemberPersistenceAdapterTest {

    @Autowired
    MemberPersistenceAdapter adapter;

    @Autowired
    MemberJpaRepository repository;

    @DisplayName("회원을 저장하고 이메일로 조회한다")
    @Test
    void saveAndFind() {
        Member saved = adapter.save(Member.register(Email.from("a@example.com"), Nickname.from("에이"), "hash", LocalDateTime.now()));

        Assertions.assertThat(saved.getId()).isNotNull();
        Assertions.assertThat(adapter.findByEmail(Email.from("A@Example.com"))).contains(saved);
        Assertions.assertThat(adapter.existsByNickname(Nickname.from("에이"))).isTrue();
    }

    @DisplayName("이메일, 닉네임 중복은 저장소 제약으로 막는다")
    @Test
    void uniqueConstraints() {
        adapter.save(Member.register(Email.from("a@example.com"), Nickname.from("에이"), "hash", LocalDateTime.now()));
        repository.flush();

        Assertions.assertThatThrownBy(() -> {
            adapter.save(Member.register(Email.from("a@example.com"), Nickname.from("비이"), "hash", LocalDateTime.now()));
            repository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
