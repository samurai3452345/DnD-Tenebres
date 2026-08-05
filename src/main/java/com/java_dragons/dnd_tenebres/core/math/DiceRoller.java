package com.java_dragons.dnd_tenebres.core.math;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class DiceRoller {

    private static final Set<Integer> ALLOWED_SIDES = Set.of(4, 6, 8, 10, 12, 20, 100);

    private DiceRoller() {}

    public static int roll(int count, int sides) {
        if (count <= 0) {
            throw new IllegalArgumentException("Количество кубиков должно быть больше 0");
        }

        if (!ALLOWED_SIDES.contains(sides)) {
            throw new IllegalArgumentException("Недопустимое количество граней: " + sides);
        }

        int sum = 0;
        for (int i = 0; i < count; i++) {
            sum += ThreadLocalRandom.current().nextInt(1, sides + 1);
        }

        return sum;
    }

    public static int rollD20() {
        return roll(1, 20);
    }

    public static int rollD100() {
        return roll(1, 100);
    }
}