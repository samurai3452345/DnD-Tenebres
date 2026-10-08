package com.java_dragons.dnd_tenebres.domain.combat.entity;

import com.java_dragons.dnd_tenebres.domain.combat.model.*;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.entity.PlayerStats;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CombatEncounterTest {
    @Test
    void проходитПолныйЖизненныйЦиклСНесколькимиВрагами() {
        CombatEncounter encounter = CombatEncounter.start(player(), EncounterReason.HUNT);
        CombatParticipant first = participant(encounter, monster("Первый"), 0, ParticipantStatus.ACTIVE);
        CombatParticipant second = participant(encounter, monster("Второй"), 1, ParticipantStatus.WAITING);
        encounter.addParticipant(first);
        encounter.addParticipant(second);

        assertThat(encounter.currentParticipant()).contains(first);
        first.markDead();
        second.activate();
        encounter.advanceRound();
        assertThat(encounter.currentParticipant()).contains(second);
        assertThat(encounter.getRound()).isEqualTo(2);

        second.markDead();
        encounter.finish(EncounterStatus.VICTORY);
        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.VICTORY);
        assertThat(encounter.getActivePlayerKey()).isNull();
        assertThatThrownBy(encounter::advanceRound).isInstanceOf(IllegalStateException.class);
    }

    private CombatParticipant participant(CombatEncounter encounter, Monster monster, int order, ParticipantStatus status) {
        return CombatParticipant.builder().encounter(encounter).monster(monster).turnOrder(order).status(status).build();
    }

    private Monster monster(String name) {
        return Monster.builder().name(name).templateName(name).maxHp(10).armorClass(10).build();
    }

    private Player player() {
        return Player.builder().id(7L).name("Герой").accountId(1L).maxHp(10).maxMp(10)
                .stats(new PlayerStats(10, 10, 10, 10, 10, 10)).build();
    }
}
