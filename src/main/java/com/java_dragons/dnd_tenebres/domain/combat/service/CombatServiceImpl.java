package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.core.math.ItemProgressionCalculator;
import com.java_dragons.dnd_tenebres.core.math.StatMathUtils;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageCalculator;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.entity.Weapon;
import com.java_dragons.dnd_tenebres.domain.monster.entity.MonsterMock;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CombatServiceImpl implements CombatService {
    private final DamageCalculator damageCalculator;
    private final ItemProgressionCalculator itemProgressionCalculator;

    @Override
    public String executeAttack(Player attacker, Weapon weapon, MonsterMock defender) {
        int d20 = DiceRoller.rollD20();

        if (d20 == 1) {
            return String.format("Критический промах! %s спотыкается и промахивается по %s.",
                    attacker.getName(), defender.getName());
        }

        boolean isCrit = (d20 == 20);

        int strModifier = StatMathUtils.calculateModifier(attacker.getStats().getStrength());

        if (!isCrit) {
            int attackRoll = d20 + strModifier;
            if (attackRoll < defender.getArmorClass()) {
                return String.format("Промах! %s уклонился. ", defender.getName());
            }
        }

        int tier = weapon.getCurrentTier(itemProgressionCalculator);
        int sides = weapon.getBaseDiceType().getSides();

        int rollCount = isCrit ? tier * 2 : tier;
        int baseWeaponDamage = DiceRoller.roll(rollCount, sides);

        int rarityBonus = weapon.getRarity().getFlatModifier();
        int totalBaseDamage = baseWeaponDamage +  rarityBonus + strModifier;

        int finalDamage = damageCalculator.calculateFinalDamage(
                totalBaseDamage,
                DamageType.PHYSICAL,
                defender.getElements()
        );

        defender.takeDamage(finalDamage);

        StringBuilder log = new StringBuilder();
        if (isCrit) {
            log.append("Критический удар! ");
        }

        log.append(String.format("%s наносит %d урона по %s. Осталось ХП: %d.",
                attacker.getName(), defender.getName(), finalDamage, Math.max(0, defender.getHp())));

        if (defender.isDead()) {
            log.append("Враг повержен!");
        }

        return log.toString();

    }
}
