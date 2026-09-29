package com.java_dragons.dnd_tenebres.core.random;

import org.springframework.stereotype.Component;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ThreadLocalRandomSource implements RandomSource {
    @Override
    public int nextInt(int minInclusive, int maxExclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxExclusive);
    }
}
