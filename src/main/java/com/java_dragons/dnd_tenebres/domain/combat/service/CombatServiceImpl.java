package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.core.math.StatMathUtils;
import com.java_dragons.dnd_tenebres.domain.combat.model.CombatAction;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageCalculator;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.combat.strategy.ItemPassiveStrategy;
import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import com.java_dragons.dnd_tenebres.domain.item.service.PotionService;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CombatServiceImpl implements CombatService {

    private final DamageCalculator damageCalculator;
    private final Map<ItemPassive, ItemPassiveStrategy> passiveStrategies;
    private final PotionService potionService;

    @Autowired
    public CombatServiceImpl(DamageCalculator damageCalculator,
                             List<ItemPassiveStrategy> strategies,
                             PotionService potionService) {
        this.damageCalculator = damageCalculator;
        this.passiveStrategies = strategies.stream()
                .collect(Collectors.toMap(ItemPassiveStrategy::getTargetPassive, s -> s));
        this.potionService = potionService;
    }

    @Override
    @Transactional // ОШИБКА №4 ИСПРАВЛЕНА: Защищаем базу от сбоев
    public String executeTurn(Player player, Monster monster, int aliveEnemyCount, int round, CombatAction action, String potionTargetName) {
        StringBuilder log = new StringBuilder();
        log.append(String.format("=== Раунд %d ===\n", round));

        player.processTurnEffects(log);
        if (log.length() > 20) log.append("\n");

        // ОШИБКА №3 ИСПРАВЛЕНА: Чистый и понятный распределитель ходов (OCP)
        switch (action) {
            case ATTACK -> handlePlayerAttack(player, monster, aliveEnemyCount, log);
            case USE_POTION -> handlePotionUse(player, potionTargetName, log);
            case CAST_SPELL -> log.append("Вы пытаетесь сотворить заклинание, но магия пока недоступна!\n");
            case FLEE -> log.append("Вы пытаетесь сбежать, но двери заперты!\n");
        }

        if (monster.isDead()) {
            log.append(String.format("🏆 Враг %s рассыпается в пыль!\n", monster.getName()));
            return log.toString();
        }

        handleEnemyTurn(player, monster, round, log);

        return log.toString();
    }

    // ================= PRIVATE METHODS =================

    private void handlePlayerAttack(Player player, Monster monster, int aliveEnemyCount, StringBuilder log) {
        int d20 = DiceRoller.rollD20();
        boolean isCrit = (d20 == 20);
        int strModifier = StatMathUtils.calculateModifier(player.getTotalStrength());
        int attackRoll = d20 + strModifier;

        log.append(String.format("Ход Игрока (%s): Бросок атаки [d20: %d] + [Сила: %d] = %d против AC %d. ",
                player.getName(), d20, strModifier, attackRoll, monster.getArmorClass()));

        if (d20 == 1) {
            log.append("Критический промах! Спотыкается и теряет равновесие.\n");
            return;
        }

        if (!isCrit && attackRoll < monster.getArmorClass()) {
            log.append("Промах! Оружие скользнуло по броне врага.\n");
            return;
        }

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
            baseWeaponDamage = isCrit ? 2 : 1;
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

    private void handlePotionUse(Player player, String potionTargetName, StringBuilder log) {
        Optional<PlayerItem> potionOpt = player.getInventory().stream()
                .filter(item -> item.getTemplate().getType() == ItemType.CONSUMABLE)
                .filter(item -> item.getTemplate().getName().equalsIgnoreCase(potionTargetName))
                .filter(item -> item.getAmount() > 0)
                .findFirst();

        if (potionOpt.isPresent()) {
            PlayerItem potionItem = potionOpt.get();
            // ВАЖНО: Здесь мы передаем Template (по новой архитектуре из прошлого шага), а не строку!
            boolean applied = potionService.applyPotion(potionItem.getTemplate(), player, log);

            if (applied) {
                // ОШИБКА №2 ИСПРАВЛЕНА: Инкапсуляция. Игрок сам расходует зелье.
                player.consumeItem(potionItem);
                log.append("\n");
            } else {
                log.append("Эффект этого зелья не сработал.\n");
            }
        } else {
            log.append(String.format("Вы судорожно ищете '%s' в рюкзаке, но его там нет! Ход потрачен впустую!\n", potionTargetName));
        }
    }

    private void handleEnemyTurn(Player player, Monster monster, int round, StringBuilder log) {
        int playerAc = player.getArmorClass();
        int monsterD20 = DiceRoller.rollD20();
        int monsterAttackRoll = monsterD20 + monster.getLevel();

        log.append(String.format("Ход Врага (%s): Бросок атаки [d20: %d] + [Ур: %d] = %d против AC %d. ",
                monster.getName(), monsterD20, monster.getLevel(), monsterAttackRoll, playerAc));

        if (monsterAttackRoll < playerAc) {
            log.append(String.format("Промах! %s ловко уворачивается!\n", player.getName()));
            return;
        }

        log.append("Попадание! ");

        // ОШИБКА №1 ИСПРАВЛЕНА: Вызываем метод атаки у самого монстра (без хардкода имен)
        var attackResult = monster.performAttack(round);

        int totalMonsterDamage = attackResult.totalDamage();
        String attackName = attackResult.attackName();
        DamageType monsterDamageType = DamageType.PHYSICAL;

        for (ItemPassive passive : player.getActivePassives()) {
            if (passiveStrategies.containsKey(passive)) {
                totalMonsterDamage = passiveStrategies.get(passive)
                        .modifyIncomingDamage(player, monster, monsterDamageType, totalMonsterDamage, log);
            }
        }

        player.takeDamage(totalMonsterDamage);

        log.append(String.format("Атака: %s. Итого: %d урона. Ваше ХП: %d/%d\n",
                attackName, totalMonsterDamage, Math.max(0, player.getCurrentHp()), player.getMaxHp()));

        if (player.getCurrentHp() <= 0) {
            log.append("☠️ ТЬМА ПОГЛОЩАЕТ ВАС... ВЫ ПОГИБЛИ.\n");
        }
    }
}