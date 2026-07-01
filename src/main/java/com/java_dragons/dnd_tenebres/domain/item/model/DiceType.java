package com.java_dragons.dnd_tenebres.domain.item.model;


import lombok.Getter;

@Getter
public enum DiceType {
    D4(4),
    D6(6),
    D8(8),
    D10(10),
    D12(12);

    private final int sides;

    DiceType(int sides) {
        this.sides = sides;
    }
}
