package com.java_dragons.dnd_tenebres.core.math;

public interface ProgressionCalculator {
    long getRequiredXpForLevel(int level);
    int getHeroBaseHp(int level);

    int calculateMaxMp(int level, int intelligence);
}
