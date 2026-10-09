package com.java_dragons.dnd_tenebres;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class PostgresIntegrationTest {
    @Container static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void liquibaseAndJpaMappingsLoadOnPostgres() {
        assertThat(constraintExists("ck_players_resources")).isTrue();
        assertThat(constraintExists("uq_active_encounter_key")).isTrue();
        assertThat(constraintExists("uq_active_quest_key")).isTrue();
        assertThat(constraintExists("uq_equipped_slot_key")).isTrue();
        assertThat(columnExists("players", "active_combat_monster_id")).isFalse();
        assertThat(tableExists("items")).isFalse();
    }

    private boolean constraintExists(String name) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.table_constraints where constraint_name = ?",
                Integer.class, name);
        return count != null && count == 1;
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_name = ? and column_name = ?",
                Integer.class, table, column);
        return count != null && count == 1;
    }

    private boolean tableExists(String table) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_schema = 'public' and table_name = ?",
                Integer.class, table);
        return count != null && count == 1;
    }
}
