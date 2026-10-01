package com.java_dragons.dnd_tenebres.domain.item.dto;

import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.EquipmentSlot;
import com.java_dragons.dnd_tenebres.domain.item.model.MagicWeaponEffect;

public record InventoryItemResponse(Long id, ItemTemplateResponse template, int amount,
                                    boolean equipped, EquipmentSlot equippedSlot, boolean locked,
                                    int tier, long itemXp, ItemBonuses bonuses,
                                    MagicWeaponEffect magicEffect, DamageType magicEffectElement,
                                    int durability, int maxDurability) {
    public record ItemBonuses(int strength, int dexterity, int constitution,
                              int intelligence, int wisdom, int charisma) {
    }
}
