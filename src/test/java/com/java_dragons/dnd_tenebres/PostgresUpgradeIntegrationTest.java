package com.java_dragons.dnd_tenebres;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class PostgresUpgradeIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void обновляетСхему043ДоТекущей() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            update("db/changelog/pre-044.xml");
            assertThat(connection.isClosed()).isFalse();
            assertThat(exists(connection, "select count(*) from information_schema.columns " +
                    "where table_name='players' and column_name='active_combat_monster_id'" )).isTrue();
            update("db/changelog/db.changelog-master.xml");
            assertThat(connection.isClosed()).isFalse();
            assertThat(exists(connection, "select count(*) from information_schema.columns " +
                    "where table_name='players' and column_name='active_combat_monster_id'" )).isFalse();
        }
    }

    private void update(String changelog) throws Exception {
        // Liquibase.close() closes its JDBC connection. Keep the verification
        // connection separate so it survives both migration phases.
        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Liquibase liquibase = new Liquibase(changelog, new ClassLoaderResourceAccessor(),
                new JdbcConnection(connection))) {
            liquibase.update(new Contexts(), new LabelExpression());
        }
    }

    private boolean exists(Connection connection, String sql) throws Exception {
        try (var statement = connection.createStatement(); var result = statement.executeQuery(sql)) {
            return result.next() && result.getInt(1) == 1;
        }
    }
}
