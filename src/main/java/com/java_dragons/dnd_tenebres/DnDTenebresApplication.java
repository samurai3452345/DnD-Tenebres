package com.java_dragons.dnd_tenebres;

import com.java_dragons.dnd_tenebres.core.config.GameBalanceProperties;
import com.java_dragons.dnd_tenebres.core.config.GamePlayerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({GamePlayerProperties.class, GameBalanceProperties.class})
public class DnDTenebresApplication {

    public static void main(String[] args) {
        SpringApplication.run(DnDTenebresApplication.class, args);
    }

}
