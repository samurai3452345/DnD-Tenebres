package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.core.math.StatMathUtils;
import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatReport;
import com.java_dragons.dnd_tenebres.domain.combat.entity.Spell;
import com.java_dragons.dnd_tenebres.domain.combat.model.CombatAction;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageCalculator;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.combat.repository.SpellRepository;
import com.java_dragons.dnd_tenebres.domain.combat.strategy.ItemPassiveStrategy;
import com.java_dragons.dnd_tenebres.domain.effect.model.ActiveEffect;
import com.java_dragons.dnd_tenebres.domain.effect.model.EffectTarget;
import com.java_dragons.dnd_tenebres.domain.effect.model.EffectType;
import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.DiceType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import com.java_dragons.dnd_tenebres.domain.item.model.MagicWeaponEffect;
import com.java_dragons.dnd_tenebres.domain.item.service.PotionService;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.model.MonsterSkill;
import com.java_dragons.dnd_tenebres.domain.monster.strategy.MonsterSkillStrategy;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class CombatServiceImpl implements CombatService {

    private final DamageCalculator damageCalculator;
    private final Map<ItemPassive, ItemPassiveStrategy> passiveStrategies;
    private final Map<MonsterSkill, MonsterSkillStrategy> monsterSkillStrategies;
    private final PotionService potionService;
    private final SpellRepository spellRepository;

    @Autowired
    public CombatServiceImpl(DamageCalculator damageCalculator,
                             List<ItemPassiveStrategy> itemStrategies,
                             List<MonsterSkillStrategy> monsterStrategies,
                             PotionService potionService,
                             SpellRepository spellRepository) {
        this.damageCalculator = damageCalculator;
        this.passiveStrategies = itemStrategies.stream()
                .collect(Collectors.toMap(ItemPassiveStrategy::getTargetPassive, s -> s));
        this.monsterSkillStrategies = monsterStrategies.stream()
                .collect(Collectors.toMap(MonsterSkillStrategy::getTargetSkill, s -> s));
        this.potionService = potionService;
        this.spellRepository = spellRepository;
    }

    @Override
    @Transactional
    public CombatReport executeTurn(Player player, Monster monster, int aliveEnemyCount, int round, CombatAction action, String actionTargetName) {
        List<CombatEvent> events = new ArrayList<>();

        // 1. Процессим эффекты перед ходом (Регенерация, Урон от кровотечения)
        player.processTurnEffects(events);
        processMonsterTurnEffects(monster, events);

        if (monster.isDead()) {
            events.add(new CombatEvent(monster.getName(), "DEATH", monster.getName(), 0, "Монстр погиб от периодического урона!"));
            return new CombatReport(round, events, true, false);
        }

        switch (action) {
            case ATTACK -> handlePlayerAttack(player, monster, aliveEnemyCount, events);
            case USE_POTION -> handlePotionUse(player, actionTargetName, events);
            case CAST_SPELL -> handlePlayerCastSpell(player, monster, aliveEnemyCount, actionTargetName, events);
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

    private void handlePlayerCastSpell(Player player, Monster monster, int aliveEnemyCount, String spellName, List<CombatEvent> events) {
        Optional<PlayerItem> magicWeaponOpt = player.getMainHandWeapon()
                .filter(item -> item.getTemplate().getType() == ItemType.MAGIC_WEAPON);

        if (magicWeaponOpt.isEmpty()) {
            events.add(new CombatEvent(player.getName(), "FAIL", monster.getName(), 0, "В руках нет магического оружия"));
            return;
        }

        PlayerItem weapon = magicWeaponOpt.get();
        int maxTier = weapon.getTemplate().getRarity().getTierIndex();
        MagicWeaponEffect weaponEffect = weapon.getMagicEffect();

        Spell spellToCast;

        if (weaponEffect == MagicWeaponEffect.CHAOS) {
            List<Spell> chaosSpells = spellRepository.findByTier(maxTier);
            if (chaosSpells.isEmpty()) return;
            spellToCast = chaosSpells.get(ThreadLocalRandom.current().nextInt(chaosSpells.size()));
            events.add(new CombatEvent(player.getName(), "CHAOS_PROC", monster.getName(), 0, "Магия Хаоса выбрала: " + spellToCast.getName()));
        } else {
            Optional<Spell> spellOpt = spellRepository.findByName(spellName);
            if (spellOpt.isEmpty() || spellOpt.get().getTier() > maxTier) {
                events.add(new CombatEvent(player.getName(), "FAIL", monster.getName(), 0, "Заклинание недоступно"));
                return;
            }
            spellToCast = spellOpt.get();
        }

        int manaCost = spellToCast.getManaCost();
        if (weaponEffect == MagicWeaponEffect.MANA_DISCOUNT) manaCost = (int) (manaCost * 0.8);

        if (!player.spendMp(manaCost)) {
            events.add(new CombatEvent(player.getName(), "FAIL", monster.getName(), 0, "Недостаточно маны"));
            return;
        }

        int d20 = DiceRoller.rollD20();
        int intModifier = StatMathUtils.calculateModifier(player.getTotalIntelligence());
        int attackRoll = d20 + intModifier;
        if (weaponEffect == MagicWeaponEffect.HOMING) attackRoll += 5;

        if (d20 == 1 || (d20 != 20 && attackRoll < monster.getArmorClass())) {
            events.add(new CombatEvent(player.getName(), "MISS", monster.getName(), 0, "Заклинание ушло в молоко"));
            return;
        }

        boolean isCrit = (d20 == 20);
        int finalDamage = calculateSpellDamage(spellToCast, player, monster, weapon, isCrit, events);

        if (finalDamage > 0) {
            monster.takeDamage(finalDamage, spellToCast.getElement());
            events.add(new CombatEvent(player.getName(), isCrit ? "CRIT_SPELL" : "CAST_SPELL", monster.getName(), finalDamage, spellToCast.getName()));
        }

        applySpellEffect(spellToCast, player, monster, finalDamage, events);
    }

    private int calculateSpellDamage(Spell spell, Player player, Monster monster, PlayerItem weapon, boolean isCrit, List<CombatEvent> events) {
        if (spell.getDamageDice() == null || spell.getDiceCount() == 0) return 0;

        int diceCount = isCrit ? spell.getDiceCount() * 2 : spell.getDiceCount();
        int baseDamage = DiceRoller.roll(diceCount, spell.getDamageDice().getSides()) + spell.getFlatBonus();
        int rarityBonus = weapon.getTemplate().getRarity().getFlatModifier();
        int totalDamage = baseDamage + rarityBonus + StatMathUtils.calculateModifier(player.getTotalIntelligence());

        double multiplier = 1.0;
        if (weapon.getMagicEffect() == MagicWeaponEffect.SPELL_POWER) multiplier += 0.10;
        if (weapon.getMagicEffect() == MagicWeaponEffect.ELEMENTAL_MASTERY && spell.getElement() == weapon.getMagicEffectElement()) multiplier += 0.20;
        if (weapon.getMagicEffect() == MagicWeaponEffect.CHAOS) multiplier += 0.40;

        if (monster.getCombatEffects().stream().anyMatch(e -> e.getType() == EffectType.LIGHT_MARK)) {
            multiplier += 0.20;
            events.add(new CombatEvent(monster.getName(), "EFFECT_TRIGGER", monster.getName(), 0, "Метка Света увеличивает урон!"));
        }

        return damageCalculator.calculateFinalDamage((int) (totalDamage * multiplier), spell.getElement(), monster.getElements());
    }

    private void applySpellEffect(Spell spell, Player player, Monster monster, int damageDealt, List<CombatEvent> events) {
        if (spell.getEffectType() == null || spell.getEffectType() == EffectType.NONE) return;

        if (spell.getEffectType() == EffectType.LIFESTEAL) {
            int healAmount = damageDealt / 2;
            player.heal(healAmount);
            events.add(new CombatEvent(player.getName(), "HEAL", player.getName(), healAmount, "Истощение (Вампиризм)"));
            return;
        }

        ActiveEffect newEffect = new ActiveEffect(spell.getEffectType(), spell.getEffectDuration(), spell.getEffectPower());

        if (spell.getEffectTarget() == EffectTarget.ENEMY) {
            monster.addCombatEffect(newEffect);
            events.add(new CombatEvent(player.getName(), "APPLY_DEBUFF", monster.getName(), spell.getEffectPower(), spell.getEffectType().name()));
        } else if (spell.getEffectTarget() == EffectTarget.CASTER) {
            player.addEffect(newEffect);
            events.add(new CombatEvent(player.getName(), "APPLY_BUFF", player.getName(), spell.getEffectPower(), spell.getEffectType().name()));
        }
    }

    private void handleEnemyTurn(Player player, Monster monster, int round, List<CombatEvent> events) {
        boolean isStunned = monster.getCombatEffects().stream()
                .anyMatch(e -> e.getType() == EffectType.FREEZE || e.getType() == EffectType.SUPPRESSION || e.getType() == EffectType.BLIND);

        if (isStunned) {
            events.add(new CombatEvent(monster.getName(), "SKIP_TURN", player.getName(), 0, "Монстр пропускает ход из-за эффекта контроля"));
            return;
        }

        int playerAc = player.getArmorClass();

        Optional<ActiveEffect> shieldOpt = player.getActiveEffects().stream().filter(e -> e.getType() == EffectType.PROTECTION_UP).findFirst();
        if (shieldOpt.isPresent()) playerAc += shieldOpt.get().getPower();

        int monsterD20 = DiceRoller.rollD20();

        if (monster.getCombatEffects().stream().anyMatch(e -> e.getType() == EffectType.SHOCK)) {
            monsterD20 /= 2;
            events.add(new CombatEvent(monster.getName(), "EFFECT_TRIGGER", monster.getName(), 0, "Шок снижает точность врага"));
        }

        Optional<ActiveEffect> frostOpt = monster.getCombatEffects().stream().filter(e -> e.getType() == EffectType.FROSTBITE).findFirst();
        if (frostOpt.isPresent()) {
            monsterD20 -= frostOpt.get().getPower();
            events.add(new CombatEvent(monster.getName(), "EFFECT_TRIGGER", monster.getName(), 0, "Обморожение сковывает движения"));
        }

        if ((monsterD20 + monster.getLevel()) < playerAc) {
            events.add(new CombatEvent(monster.getName(), "MISS", player.getName(), 0, "Промах"));
            return;
        }

        var attackResult = monster.performAttack(round, monsterSkillStrategies.get(monster.getSpecialSkill()));
        int damage = attackResult.totalDamage();

        Optional<ActiveEffect> absShield = player.getActiveEffects().stream().filter(e -> e.getType() == EffectType.ABSOLUTE_SHIELD).findFirst();
        if (absShield.isPresent()) {
            events.add(new CombatEvent(player.getName(), "BLOCK", monster.getName(), damage, "Абсолютный щит поглотил урон!"));
            absShield.get().decrementDuration();
            if (absShield.get().getDuration() <= 0) player.removeEffect(EffectType.ABSOLUTE_SHIELD);
            return;
        }

        if (player.hasEffect(EffectType.DAMAGE_REDUCTION)) {
            int originalDamage = damage;
            damage = (int) (damage * 0.1);
            events.add(new CombatEvent(player.getName(), "EFFECT_TRIGGER", monster.getName(), originalDamage - damage, "Аура Неприкосновенности поглощает 90% урона!"));
        }

        Optional<ActiveEffect> shieldHpOpt = player.getActiveEffects().stream().filter(e -> e.getType() == EffectType.SHIELD_HP).findFirst();
        if (shieldHpOpt.isPresent()) {
            ActiveEffect shield = shieldHpOpt.get();
            if (damage <= shield.getPower()) {
                shield.reducePower(damage); // Тот самый наш бизнес-метод!
                events.add(new CombatEvent(player.getName(), "BLOCK", monster.getName(), damage, "Магический щит принял удар"));
                damage = 0;
            } else {
                int blocked = shield.getPower();
                damage -= blocked;
                shield.reducePower(blocked);
                player.removeEffect(EffectType.SHIELD_HP);
                events.add(new CombatEvent(player.getName(), "BLOCK", monster.getName(), blocked, "Магический щит разбит, но часть урона поглощена"));
            }
        }

        Optional<ActiveEffect> golem = player.getActiveEffects().stream().filter(e -> e.getType() == EffectType.GOLEM).findFirst();
        if (golem.isPresent() && damage > 0) {
            ActiveEffect g = golem.get();
            events.add(new CombatEvent(monster.getName(), "ATTACK_GOLEM", "Голем", damage, "Удар пришелся по голему"));

            g.reducePower(damage);

            if (g.getPower() <= 0) {
                player.removeEffect(EffectType.GOLEM);
                events.add(new CombatEvent("Голем", "DEATH", "Голем", 0, "Голем рассыпался"));
            }
            return;
        }

        if (damage > 0) {
            player.takeDamage(damage);
            events.add(new CombatEvent(monster.getName(), "ATTACK", player.getName(), damage, attackResult.attackName()));

            if (player.hasEffect(EffectType.THORNS)) {
                int reflectDamage = damage / 2;
                if (reflectDamage > 0) {
                    monster.takeDamage(reflectDamage, DamageType.NATURE);
                    events.add(new CombatEvent(player.getName(), "REFLECT", monster.getName(), reflectDamage, "Шипы возвращают урон!"));
                }
            }
        }
    }

    private void processMonsterTurnEffects(Monster monster, List<CombatEvent> events) {
        Iterator<ActiveEffect> iterator = monster.getCombatEffects().iterator();
        while (iterator.hasNext()) {
            ActiveEffect effect = iterator.next();
            if (effect.getType() == EffectType.BURN || effect.getType() == EffectType.BLEEDING) {
                monster.takeDamage(effect.getPower(), DamageType.FIRE);
                events.add(new CombatEvent(monster.getName(), "TICK_DAMAGE", monster.getName(), effect.getPower(), "Урон от статуса " + effect.getType()));
            }
            effect.decrementDuration();
            if (effect.getDuration() <= 0) iterator.remove();
        }
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

            DiceType diceType = weaponTemplate.getDamageDice();
            int diceCount = weaponTemplate.getDiceCount();

            if (diceType != null && diceCount > 0) {
                if (isCrit) diceCount *= 2;
                baseWeaponDamage = DiceRoller.roll(diceCount, diceType.getSides());
            } else {
                baseWeaponDamage = isCrit ? 2 : 1;
            }
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
}