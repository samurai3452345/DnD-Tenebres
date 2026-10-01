package com.java_dragons.dnd_tenebres.domain.combat.dto;

import com.java_dragons.dnd_tenebres.domain.combat.model.*;

import java.time.Instant;
import java.util.List;

public record CombatStateResponse(
        Long encounterId, EncounterStatus status, EncounterReason reason, int round,
        PlayerState player, EnemyState currentEnemy, long remainingEnemies,
        List<CombatAction> allowedActions, List<AbilityState> availableAbilities,
        List<PotionState> availablePotions, List<CombatEvent> events,
        List<CombatEvent> journal, Instant startedAt) {

    public record EffectState(String type, int remainingRounds, int power, String category) {}
    public record PlayerState(Long id, String name, int level, int currentHp, int maxHp,
                              int currentMp, int maxMp, String avatarKey, List<EffectState> effects) {}
    public record EnemyState(Long id, String name, int level, int currentHp, int maxHp,
                             List<String> elements, String avatarKey, List<EffectState> effects) {}
    public record AbilityState(Long id, String name, int tier, int manaCost, String element,
                               boolean available, String unavailableReason) {}
    public record PotionState(Long itemId, Long templateId, String name, int amount,
                              String action, boolean available) {}
}
