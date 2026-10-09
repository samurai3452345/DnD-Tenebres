package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CombatRewardSummaryTest {
    private CombatEvent event(String type, int value, String text) {
        return new CombatEvent("System", type, "Hero", value, text);
    }
    @Test void aggregatesRewardsAcrossEnemiesWithoutCountingDamage() {
        var rewards = CombatRewardSummary.fromEvents(List.of(
                event("REWARD", 12, "Получен опыт"), event("REWARD", 10, "Найдено золото"),
                event("LOOT", 2, "Выбито: Ткань (x2)"), event("LOOT", 3, "Выбито: Ткань (x3)"),
                event("REWARD", 7, "Найдено золото"), event("ATTACK", 55, "Hit"),
                event("LOOT", 0, "Монстр не оставил после себя ничего ценного.")));
        assertEquals(3, rewards.size());
        assertEquals(12, rewards.get(0).amount());
        assertEquals(17, rewards.get(1).amount());
        assertEquals("Золотые монеты", rewards.get(1).name());
        assertEquals(5, rewards.get(2).amount());
        assertEquals("item:Ткань", rewards.get(2).iconKey());
    }
    @Test void excludesEmptyAndUnrecognizedRewards() {
        assertTrue(CombatRewardSummary.fromEvents(List.of(event("LOOT", 1, "unknown"),
                event("REWARD", 0, "Найдено золото"))).isEmpty());
    }
}
