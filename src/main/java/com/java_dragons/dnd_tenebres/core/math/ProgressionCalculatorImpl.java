package com.java_dragons.dnd_tenebres.core.math;

import org.springframework.stereotype.Service;

@Service
public class ProgressionCalculatorImpl implements ProgressionCalculator {

    private static final long[] REQUIRED_XP_PER_LEVEL = {
            0,
            100,
            210,
            441,
            926,
            1944,
            4084,
            8576,
            18010,
            37822,
            79428,
            166798,
            350277,
            735582,
            1838956,
            4965181
    };


    @Override
    public long getRequiredXpForLevel(int level) {

        if (level < 1) {
            throw new IllegalArgumentException("Уровень не должен быть меньше 1");
        }

        if (level > REQUIRED_XP_PER_LEVEL.length) {
            throw new IllegalArgumentException("Уровень не должен быть больше 16");
        }


        return REQUIRED_XP_PER_LEVEL[level - 1];
    }
}
