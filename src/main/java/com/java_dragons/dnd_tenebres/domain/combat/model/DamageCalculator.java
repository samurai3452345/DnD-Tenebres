package com.java_dragons.dnd_tenebres.domain.combat.model;

import java.util.Set;

public interface DamageCalculator {

    int calculateFinalDamage(int baseDamage, DamageType attackType, Set<DamageType> defenderTypes);
}
