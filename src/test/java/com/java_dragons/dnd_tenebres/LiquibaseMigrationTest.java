package com.java_dragons.dnd_tenebres;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import liquibase.integration.spring.SpringLiquibase;

import static org.assertj.core.api.Assertions.assertThat;

class LiquibaseMigrationTest {

    @Test
    void appliesCompleteChangelogAndPortableConstraints() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:liquibase_test;MODE=PostgreSQL;NON_KEYWORDS=VALUE;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:/db/changelog/db.changelog-master.xml");
        liquibase.afterPropertiesSet();

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        assertThat(constraintCount(jdbc, "ck_players_resources")).isEqualTo(1);
        assertThat(constraintCount(jdbc, "uq_active_encounter_key")).isEqualTo(1);
        assertThat(constraintCount(jdbc, "uq_active_quest_key")).isEqualTo(1);
        assertThat(constraintCount(jdbc, "uq_equipped_slot_key")).isEqualTo(1);
        assertThat(jdbc.queryForObject("select is_nullable from information_schema.columns "
                + "where lower(table_name)='players' and lower(column_name)='account_id'", String.class))
                .isEqualTo("NO");
        assertThat(deleteRule(jdbc, "fk_wallet_player")).isEqualTo("SET NULL");
        assertThat(deleteRule(jdbc, "fk_encounter_player")).isEqualTo("SET NULL");
        assertThat(jdbc.queryForObject("select count(*) from information_schema.columns "
                + "where lower(table_name)='location_random_encounters' "
                + "and lower(column_name)='monster_template_name'", Integer.class)).isZero();
    }

    private int constraintCount(JdbcTemplate jdbc, String name) {
        Integer count = jdbc.queryForObject("select count(*) from information_schema.table_constraints "
                + "where lower(constraint_name)=?", Integer.class, name);
        return count == null ? 0 : count;
    }

    private String deleteRule(JdbcTemplate jdbc, String name) {
        return jdbc.queryForObject("select delete_rule from information_schema.referential_constraints "
                + "where lower(constraint_name)=?", String.class, name);
    }
}
