package com.java_dragons.dnd_tenebres.domain.item.model;

import lombok.Getter;

@Getter
public enum ItemRarity {
    COMMON(1, 0),
    UNCOMMON(2, 1),
    RARE(3,2),
    EPIC(4,4),
    LEGENDARY(5,6);

    private final int tierIndex;
    private final int flatModifier;

    ItemRarity(int tierIndex, int flatModifier) {
        this.tierIndex = tierIndex;
        this.flatModifier = flatModifier;
    }

}
