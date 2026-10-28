package com.ilililissue.member.domain.entity;

import com.ilililissue.member.domain.SelfBlockException;
import com.ilililissue.member.domain.vo.MemberId;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class MemberBlockTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 28, 9, 0);

    @DisplayName("다른 회원을 차단할 수 있다")
    @Test
    void block() {
        MemberBlock block = MemberBlock.of(new MemberId(1), new MemberId(2), NOW);

        Assertions.assertThat(block.getBlockerId()).isEqualTo(new MemberId(1));
        Assertions.assertThat(block.getBlockedId()).isEqualTo(new MemberId(2));
        Assertions.assertThat(block.getBlockedAt()).isEqualTo(NOW);
    }

    @DisplayName("자기 자신은 차단할 수 없다")
    @Test
    void cannotBlockSelf() {
        Assertions.assertThatThrownBy(() -> MemberBlock.of(new MemberId(1), new MemberId(1), NOW))
                .isInstanceOf(SelfBlockException.class);
    }
}
