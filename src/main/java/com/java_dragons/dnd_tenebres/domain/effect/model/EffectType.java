package com.java_dragons.dnd_tenebres.domain.effect.model;

import lombok.Getter;

@Getter
public enum EffectType {
    BLEEDING(EffectCategory.DEBUFF), // кровотечение наносится урон -5% в начале каждого раунда спасброски игнарируются
    WEAKNESS(EffectCategory.DEBUFF), // слабость: урон -10%
    PROTECTION_REDUCED(EffectCategory.DEBUFF), // защита: АС уменьшается -20%
    STUN(EffectCategory.NEUTRAL), // оглушение 1 ход пропускается
    MAGIC_SICKNESS(EffectCategory.DEBUFF), // Дебафф от локации ANTI_MAGIC_FIELD стоимость закленаний x2 маны

    HEAL_INSTANT(EffectCategory.BUFF),
    MANA_RESTORE(EffectCategory.BUFF),
    POISON(EffectCategory.BUFF),
    REGENERATION(EffectCategory.BUFF),
    PROTECTION_UP(EffectCategory.BUFF), // защита: АС увеличивается +10%
    TREATMENT(EffectCategory.BUFF), // лечение: каждый раунд востанавливает часть хп
    DAMAGE_UP(EffectCategory.BUFF), // увеличивает урон на +10%
    SHADOW_DEATH(EffectCategory.BUFF), //после получения смертельного урона выживаеш с 1 хп
    WELL_RESTED(EffectCategory.BUFF), // бафф от COZY_TAVERN +10 к макс хп

    BERSERKER_RAGE(EffectCategory.NEUTRAL), // увеличивается урон +20% но уменьшается класс защиты АС -15%
    BLOOD_CONTRACT(EffectCategory.NEUTRAL), // увеличивается урон +20% но накладывается кровотечение
    BURN(EffectCategory.DEBUFF),
    FROSTBITE(EffectCategory.DEBUFF),
    LIGHT_MARK(EffectCategory.BUFF),
    ABSOLUT_SHIELD(EffectCategory.BUFF),
    GOLEM(EffectCategory.NEUTRAL),
    NONE(EffectCategory.NEUTRAL);

    private final EffectCategory effectCategory;

    EffectType(EffectCategory effectCategory) {
        this.effectCategory = effectCategory;
    }
}
