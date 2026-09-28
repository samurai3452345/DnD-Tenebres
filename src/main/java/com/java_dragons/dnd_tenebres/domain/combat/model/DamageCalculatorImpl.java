package com.java_dragons.dnd_tenebres.domain.combat.model;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;


@Service
@RequiredArgsConstructor
public class DamageCalculatorImpl implements DamageCalculator {
    private final ElementalMatrix elementalMatrix;

    @Override
    public int calculateFinalDamage (int baseDamage, DamageType attackType, Set<DamageType> defenderTypes ) {
        if (baseDamage < 0) {
            throw new IllegalArgumentException("Базовый урон не может быть отрицательным");
        }

        if (defenderTypes == null) {
            throw new IllegalArgumentException("Список стихий защитника обязателен");
        }

        if (attackType == null) {
            throw new IllegalArgumentException("Тип атаки обязателен");
        }

        double totalMultiplier = defenderTypes.stream()
                .mapToDouble(type -> elementalMatrix.getMultiplier(attackType, type))
                .reduce(1.0, (a, b) -> a * b);

        double exactDamage = baseDamage * totalMultiplier;
        long roundedDamage = Math.round(exactDamage);

        return (int) roundedDamage;
    }

}
