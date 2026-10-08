package com.java_dragons.dnd_tenebres;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:dnd_test;MODE=PostgreSQL;NON_KEYWORDS=VALUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.liquibase.enabled=true",
        "application.security.jwt.secret=тестовый-секрет-длиной-больше-тридцати-двух-байт"
})
class DnDTenebresApplicationTests {

    @Test
    void contextLoads() {
    }

}
