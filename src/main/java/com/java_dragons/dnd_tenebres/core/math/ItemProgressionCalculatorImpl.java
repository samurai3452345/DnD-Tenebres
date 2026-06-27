package com.java_dragons.dnd_tenebres.core.math;

import com.java_dragons.dnd_tenebres.domain.item.model.ItemRarity;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import org.springframework.stereotype.Service;

@Service
public class ItemProgressionCalculatorImpl implements ItemProgressionCalculator {

    private static final int TIER_2_THRESHOLD = 200;
    private static final int TIER_3_THRESHOLD = 1000;
    private static final int TIER_4_THRESHOLD = 4000;

    @Override
    public int calculateXpYield(ItemType targetType, ItemRarity targetRarity, ItemType foodType, ItemRarity foodRarity) {
        if (targetType == null || targetRarity == null || foodType == null || foodRarity == null) {
            throw new IllegalArgumentException("Параметры предметов не могут быть null");
        }

        int baseXp = getBaseFoodXp(foodRarity);
        double typeMultiplier = (targetType == foodType) ? 1.5 : 1.0;

        int delta = foodRarity.getTierIndex() - targetRarity.getTierIndex();
        double rarityMultiplier = getRarityMultiplier(delta);

        double exactYield = baseXp * typeMultiplier * rarityMultiplier;

        return (int) exactYield;

    }

    @Override
    public int getTierByXp(long currentXp) {
        if (currentXp < 0) {
            throw new IllegalArgumentException("Опыт предмета не может быть отрицательным: " + currentXp);
        }
        if (currentXp < TIER_2_THRESHOLD) return 1;
        if (currentXp < TIER_3_THRESHOLD) return 2;
        if (currentXp < TIER_4_THRESHOLD) return 3;

        return 4;
    }

    @Override
    public int getRequiredHeroLevelForTier(int tier) {
        return switch (tier) {
            case 1 -> 1;
            case 2 -> 5;
            case 3 -> 9;
            case 4 -> 13;
            default -> throw new IllegalArgumentException("Неизвестный тир предмета " + tier);
        };
    }

    private int getBaseFoodXp(ItemRarity foodRarity) {
        return switch (foodRarity) {
            case COMMON -> 10;
            case UNCOMMON -> 30;
            case RARE -> 150;
            case EPIC -> 800;
            case LEGENDARY -> 3000;
        };
    }

    private double getRarityMultiplier(int delta) {
        if (delta > 0) return 1.2;
        if (delta == 0) return 1.0;
        if (delta == -1) return 0.5;

        return 0.1;
    }

}
