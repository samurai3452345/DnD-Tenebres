package com.java_dragons.dnd_tenebres.domain.combat.model;

import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class ElementalMatrixImpl implements ElementalMatrix {
    private final Map<DamageType, Map<DamageType, Double>> matrix;

    public ElementalMatrixImpl() {
        this.matrix = new EnumMap<>(DamageType.class);

        for (DamageType attack : DamageType.values()) {
            Map<DamageType, Double> defenseMap = new EnumMap<>(DamageType.class);
            for (DamageType defense : DamageType.values()) {
                defenseMap.put(defense, 1.0);
            }
            matrix.put(attack, defenseMap);
        }

        setupElementalRules();
    }

    private void setupElementalRules() {
        setRule(DamageType.FIRE, DamageType.WATER, 0.5);
        setRule(DamageType.FIRE, DamageType.AIR, 0.5);
        setRule(DamageType.FIRE, DamageType.NATURE, 2.0);
        setRule(DamageType.FIRE, DamageType.ICE, 2.0);

        setRule(DamageType.WATER, DamageType.FIRE, 2.0);
        setRule(DamageType.WATER, DamageType.EARTH, 2.0);
        setRule(DamageType.WATER, DamageType.ELECTRICITY, 0.5);
        setRule(DamageType.WATER, DamageType.ICE, 0.5);

        setRule(DamageType.ELECTRICITY, DamageType.WATER, 2.0);
        setRule(DamageType.ELECTRICITY, DamageType.AIR, 2.0);
        setRule(DamageType.ELECTRICITY, DamageType.EARTH, 0.5);
        setRule(DamageType.ELECTRICITY, DamageType.NATURE, 0.5);

        setRule(DamageType.EARTH, DamageType.ELECTRICITY, 2.0);
        setRule(DamageType.EARTH, DamageType.AIR, 2.0);
        setRule(DamageType.EARTH, DamageType.WATER, 0.5);
        setRule(DamageType.EARTH, DamageType.NATURE, 0.5);

        setRule(DamageType.NATURE, DamageType.EARTH, 2.0);
        setRule(DamageType.NATURE, DamageType.ELECTRICITY, 2.0);
        setRule(DamageType.NATURE, DamageType.FIRE, 0.5);
        setRule(DamageType.NATURE, DamageType.ICE, 0.5);

        setRule(DamageType.ICE, DamageType.WATER, 2.0);
        setRule(DamageType.ICE, DamageType.NATURE, 2.0);
        setRule(DamageType.ICE, DamageType.AIR, 0.5);
        setRule(DamageType.ICE, DamageType.FIRE, 0.5);

        setRule(DamageType.AIR, DamageType.ICE, 2.0);
        setRule(DamageType.AIR, DamageType.FIRE, 2.0);
        setRule(DamageType.AIR, DamageType.EARTH, 0.5);
        setRule(DamageType.AIR, DamageType.ELECTRICITY, 0.5);

        setRule(DamageType.LIGHT, DamageType.DARK, 2.0);
        setRule(DamageType.DARK, DamageType.LIGHT, 2.0);
    }

    private void setRule(DamageType attack, DamageType defense, double multiplier) {
        matrix.get(attack).put(defense, multiplier);
    }

    @Override
    public double getMultiplier(DamageType attacker, DamageType defender) {
        if (attacker == null || defender == null) {
            throw new IllegalArgumentException("Типы стихий не могут быть null");
        }
        return matrix.get(attacker).get(defender);
    }


}

