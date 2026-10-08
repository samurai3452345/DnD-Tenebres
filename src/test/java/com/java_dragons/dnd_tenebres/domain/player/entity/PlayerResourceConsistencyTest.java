package com.java_dragons.dnd_tenebres.domain.player.entity;

import com.java_dragons.dnd_tenebres.domain.effect.model.ActiveEffect;
import com.java_dragons.dnd_tenebres.domain.effect.model.EffectType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerResourceConsistencyTest {

    @Test
    void динамическоеСнижениеМаксимумаСразуНормализуетТекущуюМану() {
        Player player = player();

        player.addEffect(new ActiveEffect(EffectType.MAGIC_SICKNESS, 3, 50));

        assertThat(player.getMaxMp()).isEqualTo(10);
        assertThat(player.getCurrentMp()).isEqualTo(10);

        player.removeEffect(EffectType.MAGIC_SICKNESS);

        assertThat(player.getMaxMp()).isEqualTo(20);
        assertThat(player.getCurrentMp()).isEqualTo(10);
    }

    private Player player() {
        return Player.builder()
                .name("Hero")
                .level(1)
                .currentHp(20)
                .maxHp(20)
                .currentMp(20)
                .maxMp(20)
                .stats(PlayerStats.builder().strength(10).dexterity(10).constitution(10)
                        .intelligence(10).wisdom(10).charisma(10).build())
                .build();
    }

}
