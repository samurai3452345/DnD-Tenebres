package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.core.math.StatMathUtils;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageCalculator;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.combat.strategy.ItemPassiveStrategy;
import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CombatServiceImpl implements CombatService {

    private final DamageCalculator damageCalculator;
    private final Map<ItemPassive, ItemPassiveStrategy> passiveStrategies;

    @org.springframework.beans.factory.annotation.Autowired
    public CombatServiceImpl(DamageCalculator damageCalculator, List<ItemPassiveStrategy> strategies) {
        this.damageCalculator = damageCalculator;
        this.passiveStrategies = strategies.stream()
                .collect(Collectors.toMap(ItemPassiveStrategy::getTargetPassive, s -> s));
    }

    @Override
    public String executeRound(Player player, Monster monster, int aliveEnemyCount, int round) {
        StringBuilder log = new StringBuilder();


        int d20 = DiceRoller.rollD20();
        boolean isCrit = (d20 == 20);
        int strModifier = StatMathUtils.calculateModifier(player.getTotalStrength());        int attackRoll = d20 + strModifier;

        log.append(String.format("Ход Игрока (%s): Бросок атаки [d20: %d] + [Сила: %d] = %d против AC %d. ",
                player.getName(), d20, strModifier, attackRoll, monster.getArmorClass()));

        if (d20 == 1) {
            log.append("Критический промах! Спотыкается и теряет равновесие.\n");
        } else if (!isCrit && attackRoll < monster.getArmorClass()) {
            log.append("Промах! Оружие скользнуло по броне врага.\n");
        } else {
            if (isCrit) log.append("КРИТИЧЕСКОЕ ПОПАДАНИЕ! ");
            else log.append("Попадание! ");

            Optional<PlayerItem> weaponOpt = player.getMainHandWeapon();
            int baseWeaponDamage = 0;
            int rarityBonus = 0;
            String weaponName = "Голые кулаки";

            if (weaponOpt.isPresent()) {
                ItemTemplate weaponTemplate = weaponOpt.get().getTemplate();
                weaponName = weaponTemplate.getName();
                rarityBonus = weaponTemplate.getRarity().getFlatModifier();

                String[] diceParts = weaponTemplate.getDamageDice().toLowerCase().split("d");
                int diceCount = Integer.parseInt(diceParts[0]);
                int diceSides = Integer.parseInt(diceParts[1]);

                if (isCrit) diceCount *= 2;

                baseWeaponDamage = DiceRoller.roll(diceCount, diceSides);
            } else {
                baseWeaponDamage = isCrit ? 2 : 1; // Урон голыми руками
            }

            int totalBaseDamage = baseWeaponDamage + rarityBonus + strModifier;

            DamageType playerDamageType = DamageType.PHYSICAL;
            for (ItemPassive passive : player.getActivePassives()) {
                if (passiveStrategies.containsKey(passive)) {
                    totalBaseDamage = passiveStrategies.get(passive)
                            .modifyOutgoingDamage(player, monster, aliveEnemyCount, playerDamageType, totalBaseDamage, log);
                }
            }

            int finalDamage = damageCalculator.calculateFinalDamage(totalBaseDamage, playerDamageType, monster.getElements());

            monster.takeDamage(finalDamage);

            log.append(String.format("Оружие: %s. [Урон на дайсах: %d] + [Модификаторы: %d]. Итого: %d урона. ХП врага: %d/%d\n",
                    weaponName, baseWeaponDamage, (rarityBonus + strModifier), finalDamage, Math.max(0, monster.getCurrentHp()), monster.getMaxHp()));
        }

        if (monster.isDead()) {
            log.append(String.format("🏆 Враг %s рассыпается в пыль!\n", monster.getName()));
            return log.toString();
        }

        int playerAc = player.getArmorClass();

        int monsterD20 = DiceRoller.rollD20();
        int monsterAttackRoll = monsterD20 + monster.getLevel(); // Чем выше уровень монстра, тем точнее он бьет

        log.append(String.format("Ход Врага (%s): Бросок атаки [d20: %d] + [Ур: %d] = %d против AC %d. ",
                monster.getName(), monsterD20, monster.getLevel(), monsterAttackRoll, playerAc));

        if (monsterAttackRoll < playerAc) {
            log.append(String.format("Промах! %s ловко уворачивается!\n", player.getName()));
        } else {
            log.append("Попадание! ");

            int monsterDiceDamage = DiceRoller.roll(1, monster.getDamageDice().getSides());
            int totalMonsterDamage = monsterDiceDamage + monster.getDamageBonus();
            String attackName = monster.getAttackName();

            DamageType monsterDamageType = DamageType.PHYSICAL;

            if (monster.getName().equals("Скелет-страж") && round % 3 == 0) {
                totalMonsterDamage = (int) (totalMonsterDamage * 1.5);
                attackName = "КРУГОВОЙ УДАР";
            }

            for (ItemPassive passive : player.getActivePassives()) {
                if (passiveStrategies.containsKey(passive)) {
                    totalMonsterDamage = passiveStrategies.get(passive)
                            .modifyIncomingDamage(player, monster, monsterDamageType, totalMonsterDamage, log);
                }
            }

            player.takeDamage(totalMonsterDamage);

            log.append(String.format("Атака: %s. [Урон на дайсе: %d] + [Бонус: %d]. Итого: %d урона. Ваше ХП: %d/%d\n",
                    attackName, monsterDiceDamage, monster.getDamageBonus(), totalMonsterDamage, Math.max(0, player.getCurrentHp()), player.getMaxHp()));

            if (player.getCurrentHp() <= 0) {
                log.append("☠️ ТЬМА ПОГЛОЩАЕТ ВАС... ВЫ ПОГИБЛИ.\n");
            }
        }

        return log.toString();
    }
}