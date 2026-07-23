package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.core.math.StatMathUtils;
import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatReport;
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
import com.java_dragons.dnd_tenebres.domain.monster.model.MonsterSkill;
import com.java_dragons.dnd_tenebres.domain.monster.strategy.MonsterSkillStrategy;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CombatServiceImpl implements CombatService {

    private final DamageCalculator damageCalculator;
    private final Map<ItemPassive, ItemPassiveStrategy> passiveStrategies;
    private final Map<MonsterSkill, MonsterSkillStrategy> monsterSkillStrategies;
    private final PotionService potionService;

    // ВАЖНО: Добавлен аргумент List<MonsterSkillStrategy> monsterStrategies
    @Autowired
    public CombatServiceImpl(DamageCalculator damageCalculator,
                             List<ItemPassiveStrategy> itemStrategies,
                             List<MonsterSkillStrategy> monsterStrategies,
                             PotionService potionService) {
        this.damageCalculator = damageCalculator;
        this.passiveStrategies = itemStrategies.stream()
                .collect(Collectors.toMap(ItemPassiveStrategy::getTargetPassive, s -> s));
        this.monsterSkillStrategies = monsterStrategies.stream()
                .collect(Collectors.toMap(MonsterSkillStrategy::getTargetSkill, s -> s));
        this.potionService = potionService;
    }

    @Override
    @Transactional
    public CombatReport executeTurn(Player player, Monster monster, int aliveEnemyCount, int round, CombatAction action, String potionTargetName) {
        List<CombatEvent> events = new ArrayList<>();

        player.processTurnEffects(events);

        switch (action) {
            case ATTACK -> handlePlayerAttack(player, monster, aliveEnemyCount, events);
            case USE_POTION -> handlePotionUse(player, potionTargetName, events);
            case CAST_SPELL -> events.add(new CombatEvent(player.getName(), "FAIL", monster.getName(), 0, "Магия недоступна"));
            case FLEE -> events.add(new CombatEvent(player.getName(), "FAIL", monster.getName(), 0, "Двери заперты"));
        }

        if (monster.isDead()) {
            events.add(new CombatEvent(monster.getName(), "DEATH", monster.getName(), 0, "Враг повержен"));
            return new CombatReport(round, events, true, false);
        }

        handleEnemyTurn(player, monster, round, events);

        boolean isPlayerDead = player.getCurrentHp() <= 0;
        if (isPlayerDead) {
            events.add(new CombatEvent(player.getName(), "DEATH", player.getName(), 0, "Вы погибли"));
        }

        return new CombatReport(round, events, false, isPlayerDead);
    }

    private void handlePlayerAttack(Player player, Monster monster, int aliveEnemyCount, List<CombatEvent> events) {
        int d20 = DiceRoller.rollD20();
        boolean isCrit = (d20 == 20);
        int strModifier = StatMathUtils.calculateModifier(player.getTotalStrength());
        int attackRoll = d20 + strModifier;

        if (d20 == 1) {
            events.add(new CombatEvent(player.getName(), "MISS", monster.getName(), 0, "Критический промах"));
            return;
        }

        if (!isCrit && attackRoll < monster.getArmorClass()) {
            events.add(new CombatEvent(player.getName(), "MISS", monster.getName(), 0, "Промах"));
            return;
        }

        Optional<PlayerItem> weaponOpt = player.getMainHandWeapon();
        int baseWeaponDamage = 0;
        int rarityBonus = 0;

        if (weaponOpt.isPresent()) {
            ItemTemplate weaponTemplate = weaponOpt.get().getTemplate();
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
                        .modifyOutgoingDamage(player, monster, aliveEnemyCount, playerDamageType, totalBaseDamage, events);
            }
        }

        int finalDamage = damageCalculator.calculateFinalDamage(totalBaseDamage, playerDamageType, monster.getElements());
        // Добавлен аргумент playerDamageType
        monster.takeDamage(finalDamage, playerDamageType);

        events.add(new CombatEvent(player.getName(), isCrit ? "CRIT_ATTACK" : "ATTACK", monster.getName(), finalDamage, "Нанесение урона"));
    }

    private void handlePotionUse(Player player, String potionTargetName, List<CombatEvent> events) {
        Optional<PlayerItem> potionOpt = player.getInventory().stream()
                .filter(item -> item.getTemplate().getType() == ItemType.CONSUMABLE)
                .filter(item -> item.getTemplate().getName().equalsIgnoreCase(potionTargetName))
                .filter(item -> item.getAmount() > 0)
                .findFirst();

        if (potionOpt.isPresent()) {
            PlayerItem potionItem = potionOpt.get();
            boolean applied = potionService.applyPotion(potionItem.getTemplate(), player, events);
            if (applied) {
                player.consumeItem(potionItem);
            }
        } else {
            events.add(new CombatEvent(player.getName(), "FAIL", player.getName(), 0, "Предмет не найден"));
        }
    }

    private void handleEnemyTurn(Player player, Monster monster, int round, List<CombatEvent> events) {
        int playerAc = player.getArmorClass();
        int monsterD20 = DiceRoller.rollD20();
        int monsterAttackRoll = monsterD20 + monster.getLevel();

        if (monsterAttackRoll < playerAc) {
            events.add(new CombatEvent(monster.getName(), "MISS", player.getName(), 0, "Промах"));
            return;
        }

        MonsterSkillStrategy currentSkillStrategy = monsterSkillStrategies.get(monster.getSpecialSkill());
        var attackResult = monster.performAttack(round, currentSkillStrategy);
        int totalMonsterDamage = attackResult.totalDamage();
        DamageType monsterDamageType = DamageType.PHYSICAL;

        for (ItemPassive passive : player.getActivePassives()) {
            if (passiveStrategies.containsKey(passive)) {
                totalMonsterDamage = passiveStrategies.get(passive)
                        .modifyIncomingDamage(player, monster, monsterDamageType, totalMonsterDamage, events);
            }
        }

        player.takeDamage(totalMonsterDamage);
        events.add(new CombatEvent(monster.getName(), "ATTACK", player.getName(), totalMonsterDamage, attackResult.attackName()));
    }
}