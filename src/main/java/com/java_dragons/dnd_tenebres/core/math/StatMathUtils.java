package com.java_dragons.dnd_tenebres.core.math;

public final class StatMathUtils {

    private StatMathUtils() {
    }

    public static int calculateModifier(int value) {
        int divident = value - 10;
        return Math.floorDiv(divident, 2);
    }


}
