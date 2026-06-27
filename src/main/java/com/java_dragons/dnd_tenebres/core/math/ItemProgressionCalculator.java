package com.java_dragons.dnd_tenebres.core.math;

import com.java_dragons.dnd_tenebres.domain.item.model.ItemRarity;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;

public interface ItemProgressionCalculator {

    int calculateXpYield(ItemType targetType, ItemRarity targetRarity, ItemType foodType, ItemRarity foodRarity);

    int getTierByXp(long currentXp);

    int getRequiredHeroLevelForTier(int tier);
}
