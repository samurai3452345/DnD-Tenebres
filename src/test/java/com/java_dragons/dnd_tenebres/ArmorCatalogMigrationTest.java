package com.java_dragons.dnd_tenebres;

import liquibase.integration.spring.SpringLiquibase;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ArmorCatalogMigrationTest {
    private static JdbcTemplate jdbc;

    @BeforeAll
    static void migrate() throws Exception {
        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:armor_catalog;MODE=PostgreSQL;NON_KEYWORDS=VALUE;DB_CLOSE_DELAY=-1");
        source.setUser("sa");
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(source);
        liquibase.setChangeLog("classpath:/db/changelog/db.changelog-master.xml");
        liquibase.afterPropertiesSet();
        jdbc = new JdbcTemplate(source);
    }

    @ParameterizedTest(name = "{0}: {3}")
    @MethodSource("newArmor")
    void createsMediumAndHeavyArmor(String name, String slot, String rarity, String armorType,
                                   int armorClass, int strength, String effect) {
        var row = jdbc.queryForMap("select * from item_templates where name=?", name);
        assertThat(row.get("TYPE")).isEqualTo("ARMOR");
        assertThat(row.get("SLOT")).isEqualTo(slot);
        assertThat(row.get("RARITY")).isEqualTo(rarity);
        assertThat(row.get("ARMOR_TYPE")).isEqualTo(armorType);
        assertThat(((Number) row.get("ARMOR_CLASS")).intValue()).isEqualTo(armorClass);
        assertThat(((Number) row.get("REQUIRED_STRENGTH")).intValue()).isEqualTo(strength);
        assertThat(((Number) row.get("STAT_BUDGET")).intValue()).isZero();
        assertThat(row.get("PASSIVE_EFFECT")).isEqualTo(effect);
        if (effect.equals("ORICHALCUM")) {
            assertThat(row.get("SET_CODE")).isEqualTo("ORICHALCUM");
        }
    }

    static Stream<Arguments> newArmor() {
        return Stream.of(
                Arguments.of("Средний мифриловый шлем", "HEAD", "RARE", "MEDIUM", 3, 10, "NONE"),
                Arguments.of("Средний мифриловый нагрудник", "CHEST", "RARE", "MEDIUM", 5, 10, "NONE"),
                Arguments.of("Средние мифриловые поножи", "LEGS", "RARE", "MEDIUM", 4, 10, "NONE"),
                Arguments.of("Тяжелый мифриловый шлем", "HEAD", "RARE", "HEAVY", 4, 15, "NONE"),
                Arguments.of("Тяжелый мифриловый нагрудник", "CHEST", "RARE", "HEAVY", 5, 15, "NONE"),
                Arguments.of("Тяжелые мифриловые поножи", "LEGS", "RARE", "HEAVY", 5, 15, "NONE"),
                Arguments.of("Средний орихалковый шлем", "HEAD", "EPIC", "MEDIUM", 4, 10, "ORICHALCUM"),
                Arguments.of("Средний орихалковый нагрудник", "CHEST", "EPIC", "MEDIUM", 5, 10, "ORICHALCUM"),
                Arguments.of("Средние орихалковые поножи", "LEGS", "EPIC", "MEDIUM", 5, 10, "ORICHALCUM"),
                Arguments.of("Тяжелый орихалковый шлем", "HEAD", "EPIC", "HEAVY", 5, 15, "ORICHALCUM"),
                Arguments.of("Тяжелый орихалковый нагрудник", "CHEST", "EPIC", "HEAVY", 6, 15, "ORICHALCUM"),
                Arguments.of("Тяжелые орихалковые поножи", "LEGS", "EPIC", "HEAVY", 5, 15, "ORICHALCUM")
        );
    }

    @Test
    void preservesLightSetsAndCreatesCompleteVariantsWithoutDuplicates() {
        for (String prefix : new String[]{"Кожан", "Желез", "Мифрил", "Орихалк"}) {
            assertThat(jdbc.queryForObject("select count(*) from item_templates where type='ARMOR' "
                    + "and armor_type='LIGHT' and name like ?", Integer.class, prefix + "%"))
                    .as("Existing light set %s", prefix).isEqualTo(3);
        }
        for (String rarity : new String[]{"RARE", "EPIC"}) {
            for (String armorType : new String[]{"MEDIUM", "HEAVY"}) {
                assertThat(jdbc.queryForList("select slot from item_templates where type='ARMOR' "
                        + "and rarity=? and armor_type=?", String.class, rarity, armorType))
                        .containsExactlyInAnyOrder("HEAD", "CHEST", "LEGS");
            }
        }
        assertThat(jdbc.queryForObject("select count(*) from (select name from item_templates "
                + "where type='ARMOR' group by name having count(*) > 1) duplicates", Integer.class)).isZero();
    }
}
