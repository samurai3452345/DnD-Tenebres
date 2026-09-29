package com.java_dragons.dnd_tenebres.domain.item.dto;

import com.java_dragons.dnd_tenebres.domain.item.model.*;

public record ItemTemplateResponse(Long id, String name, ItemType type, EquipmentSlot slot,
                                   ItemRarity rarity, ArmorType armorType, int armorClass,
                                   int requiredStrength, int requiredLevel, int maxDurability, String setCode,
                                   DiceType damageDice, int diceCount, ItemPassive passiveEffect,
                                   ConsumableAction consumableAction) {
}
