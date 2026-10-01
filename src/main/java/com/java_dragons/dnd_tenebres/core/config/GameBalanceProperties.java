package com.java_dragons.dnd_tenebres.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "game.balance")
public record GameBalanceProperties(
        int tavernCost,
        int deathGoldPenaltyPercent,
        int shortRestNeutralAmbushPercent,
        int shortRestDangerousAmbushPercent,
        int darknessAmbushBonusPercent,
        int antiMagicManaMultiplier,
        int antiMagicDamagePercent,
        int maxActiveQuests,
        int maxCombatLogPageSize) {
}
