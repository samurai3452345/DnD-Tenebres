package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.core.config.GameBalanceProperties;
import com.java_dragons.dnd_tenebres.domain.combat.entity.Spell;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.MagicWeaponEffect;
import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationEffect;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.entity.PlayerStats;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpellManaCostCalculatorTest {

    private final SpellManaCostCalculator calculator = new SpellManaCostCalculator(
            new GameBalanceProperties(10, 20, 10, 25, 15, 2, 50, 5, 100)
    );

    @Test
    void одинаковоУчитываетАнтимагиюИСкидкуФокуса() {
        Location location = new Location();
        location.setId("anti_magic");
        location.setEffect(LocationEffect.ANTI_MAGIC_FIELD);
        Player player = playerAt(location);
        Spell spell = Spell.builder().manaCost(10).build();
        PlayerItem focus = PlayerItem.builder().magicEffect(MagicWeaponEffect.MANA_DISCOUNT).build();

        assertThat(calculator.calculate(player, spell, focus)).isEqualTo(16);
    }

    @Test
    void обычнаяСтоимостьНеМеняется() {
        Spell spell = Spell.builder().manaCost(10).build();
        PlayerItem focus = PlayerItem.builder().magicEffect(MagicWeaponEffect.NONE).build();

        assertThat(calculator.calculate(playerAt(null), spell, focus)).isEqualTo(10);
    }

    private Player playerAt(Location location) {
        return Player.builder()
                .name("Mage")
                .level(1)
                .maxHp(10)
                .maxMp(10)
                .stats(PlayerStats.builder().strength(10).dexterity(10).constitution(10)
                        .intelligence(10).wisdom(10).charisma(10).build())
                .currentLocation(location)
                .build();
    }
}
