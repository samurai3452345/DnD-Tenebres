package com.java_dragons.dnd_tenebres;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import liquibase.integration.spring.SpringLiquibase;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class LiquibaseMigrationTest {

    @Test
    void referenceDataUsesNativeInsertsInsteadOfSqlBlocks() throws Exception {
        String master = readChangelog("/db/changelog/db.changelog-master.xml");
        var includes = Pattern.compile("<include file=\"([^\"]+)\"").matcher(master);
        while (includes.find()) {
            String path = includes.group(1);
            var sqlBlocks = Pattern.compile("<sql\\b[^>]*>(.*?)</sql>", Pattern.DOTALL)
                    .matcher(readChangelog(path));
            while (sqlBlocks.find()) {
                assertThat(Pattern.compile("\\bINSERT\\s+INTO\\b", Pattern.CASE_INSENSITIVE)
                        .matcher(sqlBlocks.group(1)).find())
                        .as("Seed inserts in %s must use Liquibase insert tags", path).isFalse();
            }
        }
    }

    private String readChangelog(String path) throws Exception {
        try (var stream = getClass().getResourceAsStream(path)) {
            assertThat(stream).as("Changelog %s exists", path).isNotNull();
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

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
        assertThat(jdbc.queryForList("select rarity from item_templates where name in "
                + "('Драконьи кинжалы','Ангельское копье') order by name", String.class))
                .containsExactly("LEGENDARY", "LEGENDARY");
        assertThat(jdbc.queryForObject("select count(*) from merchant_offers", Integer.class)).isEqualTo(9);
        assertThat(jdbc.queryForObject("select count(*) from merchant_offers o join item_templates t "
                + "on t.id=o.template_id where o.unit_price=t.stat_budget*2+5", Integer.class)).isEqualTo(9);
        assertWeaponRarity(jdbc, "UNCOMMON", "Парные серпы", "Тан Дао", "Ятаган", "Хопеш", "Парные косы", "Парные кинжалы");
        assertWeaponRarity(jdbc, "RARE", "Мифриловая секира", "Мифриловый полумесяц", "Мифриловый меч", "Мифриловые серпы", "Теневые кинжалы");
        assertWeaponRarity(jdbc, "EPIC", "Орихалковая рапира", "Орихалковый кинжал", "Орихалковый одноручный топор",
                "Орихалковый меч", "Орихалковый лук", "Орихалковая секира");
        assertWeaponRarity(jdbc, "LEGENDARY", "Клыки бури", "Драконья кусаригама");
        assertWeaponRarity(jdbc, "LEGENDARY", "Сёстры Погибели", "Драконий одноручный топор", "Драконий меч", "Драконий лук");
        assertThat(jdbc.queryForObject("select count(*) from item_templates where name in "
                + "('Драконьи парные мечи','Драконьи парные косы')", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from item_templates where name in "
                + "('Сёстры Погибели','Драконий одноручный топор','Драконий меч','Драконий лук') "
                + "and stat_budget=6", Integer.class)).isEqualTo(4);
        assertThat(jdbc.queryForList("select rarity from item_templates where name in "
                + "('Длинный лук','Тяжелый арбалет') order by name", String.class))
                .containsExactly("UNCOMMON", "UNCOMMON");
        assertThat(jdbc.queryForObject("select rarity from item_templates where name='Лук'", String.class))
                .isEqualTo("COMMON");
        assertThat(jdbc.queryForObject("select count(*) from item_templates where name in "
                + "('Железная руда','Мифриловая руда','Орихалковая руда') and type='RESOURCE'", Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("select count(*) from locations where id in "
                + "('forest_edge','forest_wolf_trail','forest_goblin_camp') and search_difficulty=6", Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("select count(*) from locations l where (zone_id='forgotten_crypt' or zone_id='green_forest') "
                + "and not exists (select 1 from location_loot_tables t where t.location_id=l.id)", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from location_loot_tables l join item_templates t on t.id=l.item_template_id "
                + "where t.name in ('Железный слиток','Мифриловый слиток','Орихалковый слиток')", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from location_connections where from_location_id='city_square' "
                + "and to_location_id='city_forge'", Integer.class)).isEqualTo(1);
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

    private void assertWeaponRarity(JdbcTemplate jdbc, String rarity, String... names) {
        for (String name : names) {
            var template = jdbc.queryForMap("select type, slot, rarity, damage_dice, dice_count, passive_effect "
                    + "from item_templates where name=?", name);
            assertThat(template.get("TYPE")).isEqualTo("WEAPON");
            assertThat(template.get("SLOT")).isEqualTo("MAIN_HAND");
            assertThat(template.get("RARITY")).isEqualTo(rarity);
            assertThat(template.get("PASSIVE_EFFECT")).isEqualTo("NONE");
            assertThat(template.get("DAMAGE_DICE")).isIn("D4", "D6", "D8", "D10", "D12");
            assertThat(((Number) template.get("DICE_COUNT")).intValue())
                    .as("Количество кубиков оружия %s", name).isBetween(1, 9);
        }
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
