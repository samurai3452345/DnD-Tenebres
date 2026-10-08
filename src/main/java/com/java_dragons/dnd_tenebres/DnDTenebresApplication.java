package com.java_dragons.dnd_tenebres;

import com.java_dragons.dnd_tenebres.core.config.GameBalanceProperties;
import com.java_dragons.dnd_tenebres.core.config.GamePlayerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({GamePlayerProperties.class, GameBalanceProperties.class})
public class DnDTenebresApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DnDTenebresApplication.class);
        application.addListeners(event -> {
            if (event instanceof ApplicationEnvironmentPreparedEvent preparedEvent) {
                boolean supportedProfile = java.util.Arrays.stream(preparedEvent.getEnvironment().getActiveProfiles())
                        .anyMatch(profile -> profile.equals("local") || profile.equals("production"));
                if (!supportedProfile) {
                    throw new IllegalStateException(
                            "Укажите явный профиль запуска: local или production (SPRING_PROFILES_ACTIVE)"
                    );
                }
            }
        });
        application.run(args);
    }

}
