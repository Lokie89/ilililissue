package com.ilililissue.issue.domain.vo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
public class IdTest {

    @Test
    void equals() {

        Id id1 = new Id(5);
        Id id2 = new Id(5);

        Assertions.assertThat(id1).isEqualTo(id2);

    }
}
