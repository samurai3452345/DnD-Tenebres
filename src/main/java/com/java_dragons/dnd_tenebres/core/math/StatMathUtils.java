package com.java_dragons.dnd_tenebres.core.math;

public final class StatMathUtils {

    private StatMathUtils() {
    }

    public static int calculateModifier(int value) {
        return Math.floorDiv(value-10, 2);
    }


}
