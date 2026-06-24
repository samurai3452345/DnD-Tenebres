package com.java_dragons.dnd_tenebres.domain.combat.model;

public interface ElementalMatrix {
    double getMultiplier(DamageType attacker, DamageType defender);
}
