package com.java_dragons.dnd_tenebres.core.random;

public interface RandomSource {
    int nextInt(int minInclusive, int maxExclusive);

    default int roll(int count, int sides) {
        if (count < 0 || sides <= 0) throw new IllegalArgumentException("Invalid dice");
        int result = 0;
        for (int i = 0; i < count; i++) result += nextInt(1, sides + 1);
        return result;
    }

    default boolean chance(int percent) {
        if (percent < 0 || percent > 100) throw new IllegalArgumentException("Chance must be 0..100");
        return nextInt(1, 101) <= percent;
    }
}
