package com.java_dragons.dnd_tenebres.core.math;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DiceServiceImpl implements DiceService {

    private static final Set<Integer> ALLOWED_SIDES = Set.of(4, 6, 8, 10, 12, 20);

    @Override
    public int rollDice(int count, int sides) {

        if (count <= 0) {
            throw new IllegalArgumentException("Количество кубиков должно быть больше 0");
        }

        if (!ALLOWED_SIDES.contains(sides)) {
            throw new IllegalArgumentException("Недопустимое колличество граней");
        }

        int sum = 0;
        for (int i = 0; i < count; i++) {
             sum += ThreadLocalRandom.current() .nextInt(1, sides + 1);
        }

        return sum;
    }
}
