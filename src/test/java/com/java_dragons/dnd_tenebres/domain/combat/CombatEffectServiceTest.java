package com.java_dragons.dnd_tenebres.domain.combat;

import com.java_dragons.dnd_tenebres.domain.combat.service.CombatEffectService;
import com.java_dragons.dnd_tenebres.domain.effect.model.*;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

class CombatEffectServiceTest {
    private final CombatEffectService service = new CombatEffectService();

    @Test void weaknessAndDamageBuffsModifyOutgoingDamage() {
        Player player = player();
        player.addEffect(new ActiveEffect(EffectType.WEAKNESS, 2, 10));
        assertThat(service.modifyOutgoing(player, 100, new ArrayList<>())).isEqualTo(90);
        player.removeEffect(EffectType.WEAKNESS);
        player.addEffect(new ActiveEffect(EffectType.DAMAGE_UP, 2, 20));
        assertThat(service.modifyOutgoing(player, 100, new ArrayList<>())).isEqualTo(120);
    }

    @Test void periodicEffectsTickAndExpireOnServer() {
        Player player = player();
        Monster monster = Monster.builder().name("Враг").templateName("enemy").level(1).maxHp(20).currentHp(20)
                .armorClass(10).xpReward(0).goldReward(0).build();
        player.addEffect(new ActiveEffect(EffectType.POISON, 1, 3));
        service.onRoundStart(player, monster, new ArrayList<>());
        assertThat(player.getCurrentHp()).isEqualTo(17);
        service.onRoundEnd(player, monster);
        assertThat(player.hasEffect(EffectType.POISON)).isFalse();
    }

    @Test void shadowDeathIsConsumedOnce() {
        Player player = player();
        player.addEffect(new ActiveEffect(EffectType.SHADOW_DEATH, 5, 0));
        player.takeDamage(100);
        service.preventDeath(player, new ArrayList<>());
        assertThat(player.getCurrentHp()).isEqualTo(1);
        assertThat(player.hasEffect(EffectType.SHADOW_DEATH)).isFalse();
    }

    private Player player() {
        return Player.builder().name("Герой").level(1).currentHp(20).maxHp(20).currentMp(10).maxMp(10)
                .stats(PlayerStats.builder().strength(10).dexterity(10).constitution(10).intelligence(10).wisdom(10).charisma(10).build())
                .activeEffects(new HashSet<>()).inventory(new ArrayList<>()).build();
    }
}
