package com.java_dragons.dnd_tenebres.domain.combat.entity;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatLogEntryTest {
    @Test
    void journalEventPreservesStoredRound() {
        var entry = CombatLogEntry.builder().round(3).actor("Hero").actionType("ATTACK")
                .target("Wolf").value(12).description("Hit").build();
        var event = entry.toEvent();
        assertEquals(3, event.round());
        assertEquals("Hero", event.actor());
        assertEquals(12, event.value());
    }

    @Test
    void engineCanStillCreateEventBeforeRoundIsAssigned() {
        assertNull(new CombatEvent("Hero", "ATTACK", "Wolf", 12, "Hit").round());
    }
}
