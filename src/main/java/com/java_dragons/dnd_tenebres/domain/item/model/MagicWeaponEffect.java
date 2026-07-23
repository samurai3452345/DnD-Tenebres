package com.java_dragons.dnd_tenebres.domain.item.model;

public enum MagicWeaponEffect {
    NONE,
    MANA_DISCOUNT,      // -20% от стоимости маны
    SPELL_POWER,        // +10% к урону заклинания
    HOMING,             // +5 к броску d20 на пробитие AC
    ELEMENTAL_MASTERY,  // +20% урона от заклинаний определенного элемента
    CHAOS               // Случайное заклинание того же тира с +40% урона
}