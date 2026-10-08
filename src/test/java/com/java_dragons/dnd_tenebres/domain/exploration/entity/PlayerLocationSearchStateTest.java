package com.java_dragons.dnd_tenebres.domain.exploration.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlayerLocationSearchStateTest {

    private static final Instant START = Instant.parse("2026-10-08T10:00:00Z");

    @Test
    void даётДесятьПопытокИПолностьюОбновляетИхЧерезДваЧаса() {
        PlayerLocationSearchState state = PlayerLocationSearchState.create(1L, "forest", START);

        for (int remaining = 9; remaining >= 0; remaining--) {
            assertThat(state.consume(START)).isEqualTo(remaining);
        }
        assertThatThrownBy(() -> state.consume(START.plusSeconds(7199)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(state.consume(START.plusSeconds(7200))).isEqualTo(9);
    }

    @Test
    void короткийОтдыхВозвращаетЧетыреПопыткиНоНеБольшеМаксимума() {
        PlayerLocationSearchState state = PlayerLocationSearchState.create(1L, "forest", START);
        state.consume(START);
        state.consume(START);
        state.consume(START);

        assertThat(state.restore(START, 4)).isEqualTo(10);
    }
}
