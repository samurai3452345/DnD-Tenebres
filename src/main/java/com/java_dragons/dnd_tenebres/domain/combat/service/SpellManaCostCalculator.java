package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.core.config.GameBalanceProperties;
import com.java_dragons.dnd_tenebres.domain.combat.entity.Spell;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.MagicWeaponEffect;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationEffect;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpellManaCostCalculator {

    private final GameBalanceProperties balance;

    public int calculate(Player player, Spell spell, PlayerItem magicFocus) {
        long cost = spell.getManaCost();
        if (player.getCurrentLocation() != null
                && player.getCurrentLocation().getEffect() == LocationEffect.ANTI_MAGIC_FIELD) {
            cost = Math.multiplyExact(cost, balance.antiMagicManaMultiplier());
        }
        if (magicFocus.getMagicEffect() == MagicWeaponEffect.MANA_DISCOUNT) {
            cost = cost * 80 / 100;
        }
        return Math.toIntExact(cost);
    }
}
