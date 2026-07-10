package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.core.math.ItemProgressionCalculator;
import com.java_dragons.dnd_tenebres.core.math.StatMathUtils;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageCalculator;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.entity.Weapon;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CombatServiceImpl implements CombatService {
    private final DamageCalculator damageCalculator;
    private final ItemProgressionCalculator itemProgressionCalculator;

    @Override
    public String executeRound(Player player, Weapon weapon, Monster monster, int round) {
        StringBuilder log = new StringBuilder();

        log.append("Ход Игрока: ");
        int d20 = DiceRoller.rollD20();

        if (d20 == 1) {
            log.append(String.format("Критический промах! %s спотыкается.\n", player.getName()));
        } else {
            boolean isCrit = (d20 == 20);
            int strModifier = StatMathUtils.calculateModifier(player.getStats().getStrength());
            int attackRoll = d20 + strModifier;

            if (!isCrit && attackRoll < monster.getArmorClass()) {
                log.append(String.format("Промах! %s уклонился.\n", monster.getName()));
            } else {
                int tier = weapon.getCurrentTier(itemProgressionCalculator);
                int sides = weapon.getBaseDiceType().getSides();
                int rollCount = isCrit ? tier * 2 : tier;

                int baseWeaponDamage = DiceRoller.roll(rollCount, sides);
                int rarityBonus = weapon.getRarity().getFlatModifier();
                int totalBaseDamage = baseWeaponDamage + rarityBonus + strModifier;

                int finalDamage = damageCalculator.calculateFinalDamage(
                        totalBaseDamage, DamageType.PHYSICAL, monster.getElements()
                );

                monster.takeDamage(finalDamage);

                if (isCrit) log.append("Критический удар! ");
                log.append(String.format("%s наносит %d урона по %s. Осталось ХП: %d.\n",
                        player.getName(), finalDamage, monster.getName(), Math.max(0, monster.getCurrentHp())));
            }
        }

        if (monster.isDead()) {
            log.append("🏆 Враг повержен!\n");
            return log.toString();
        }

        log.append("Ход Врага: ");

        int dexMod = StatMathUtils.calculateModifier(player.getStats().getDexterity());
        int playerAc = 10 + dexMod;

        int monsterAttackRoll = DiceRoller.rollD20() + monster.getLevel();

        if (monsterAttackRoll < playerAc) {
            log.append(String.format("%s атакует, но %s ловко уворачивается!\n", monster.getName(), player.getName()));
        } else {
            int baseDamage = DiceRoller.roll(1, monster.getDamageDice().getSides());
            int totalDamage = baseDamage + monster.getDamageBonus();
            String attackName = monster.getAttackName();

            // 🧠 ЛОГИКА УМНОГО БОССА
            if (monster.getName().equals("Скелет-страж") && round % 3 == 0) {
                totalDamage = (int) (totalDamage * 1.5);
                attackName = "ТЯЖЕЛЫЙ РАЗМАХ ☄️";
            }

            player.takeDamage(totalDamage);

            log.append(String.format("%s использует '%s' и наносит %d урона! У вас осталось %d ХП.\n",
                    monster.getName(), attackName, totalDamage, Math.max(0, player.getCurrentHp())));

            if (player.getCurrentHp() <= 0) {
                log.append("☠️ ВЫ ПОГИБЛИ...\n");
            }
        }

        return log.toString();
    }
}