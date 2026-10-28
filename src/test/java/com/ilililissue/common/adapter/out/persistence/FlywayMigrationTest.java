package com.ilililissue.common.adapter.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flyway 마이그레이션으로 만든 스키마가 JPA 엔티티 매핑과 맞는지 확인한다.
 * MySQL 호환 모드의 H2에 마이그레이션을 적용한 뒤 Hibernate가 스키마를 검증(validate)한다.
 * 엔티티를 바꾸고 마이그레이션을 추가하지 않으면 컨텍스트가 뜨지 않아 실패한다.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flyway;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class FlywayMigrationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @DisplayName("마이그레이션을 모두 적용하면 엔티티 매핑 검증을 통과한다")
    @Test
    void migrationsMatchEntities() {
        Integer applied = jdbcTemplate.queryForObject(
                "select count(*) from \"flyway_schema_history\" where \"success\" = true", Integer.class);
        assertThat(applied).isPositive();
    }
}
