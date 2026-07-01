package com.java_dragons.dnd_tenebres.core.math;

import java.util.concurrent.ThreadLocalRandom;

public class DiceRoller {
    private DiceRoller() {}

    public static int roll(int count, int sides) {
        if (count <= 0) return 0;

        int sum = 0;
        for (int i = 1; i <= count; i++) {
            sum += ThreadLocalRandom.current().nextInt(1, sides + 1);
        }
        return sum;
    }

    public static int rollD20() {
        return roll(1, 20);
    }

}
