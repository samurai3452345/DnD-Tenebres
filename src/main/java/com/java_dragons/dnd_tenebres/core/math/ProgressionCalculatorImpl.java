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

    private static final int[] HERO_BASE_HP = {
            20,
            28,
            38,
            48,
            60,
            72,
            86,
            100,
            116,
            132,
            150,
            168,
            188,
            208,
            242,
            279
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

    @Override
    public int getHeroBaseHp(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Уровень не должен быть меньше 1");
        }

        if (level >HERO_BASE_HP.length) {
            throw new IllegalArgumentException("Уровень не должен быть больше 16");
        }
        return HERO_BASE_HP[level - 1];
    }

    @Override
    public int calculateMaxMp(int level, int intelligence) {
        if (level < 1 || level > 16) {
            throw new IllegalArgumentException("Уровень должен быть от 1 до 16");
        }

        double baseMp = 20.0 + 10.0 * (level - 1) + 0.5 * Math.pow(level - 1, 2);

        int intModifier = StatMathUtils.calculateModifier(intelligence);
        int bonusMp = intModifier * 3 * level;

        int totalMp = (int) Math.floor(baseMp) + bonusMp;

        return Math.max(10, totalMp);
    }
}
