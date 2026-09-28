package com.java_dragons.dnd_tenebres.domain.combat.dto;

import com.java_dragons.dnd_tenebres.domain.combat.model.EncounterStatus;

import java.util.List;

public record CombatStateResponse(Long encounterId, EncounterStatus status, int round,
                                  PlayerState player, EnemyState currentEnemy,
                                  long remainingEnemies, List<CombatEvent> events) {
    public record PlayerState(int currentHp, int maxHp, int currentMp, int maxMp) {
    }

    public record EnemyState(Long id, String name, int currentHp, int maxHp) {
    }
}
