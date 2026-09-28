package com.java_dragons.dnd_tenebres.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @Component
@ConfigurationProperties(prefix = "game.player")
public class GamePlayerProperties {
    private String startLocationId = "city_square";
    private String respawnLocationId = "city_tavern";
    private long startGold = 50;
    private int deathGoldPenaltyPercent = 20;
    private List<StarterItem> startItems = new ArrayList<>();
    @Getter @Setter public static class StarterItem { private String template; private int amount; }
}
