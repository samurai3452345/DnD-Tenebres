package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.domain.combat.model.EncounterStatus;
import com.java_dragons.dnd_tenebres.domain.combat.repository.CombatEncounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** The encounter aggregate is the only source of truth for combat state. */
@Service
@RequiredArgsConstructor
public class CombatStateService {
    private final CombatEncounterRepository encounterRepository;

    @Transactional(readOnly = true)
    public boolean isInCombat(Long playerId) {
        return encounterRepository.existsByPlayerIdAndStatus(playerId, EncounterStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Optional<Long> activeMonsterId(Long playerId) {
        return encounterRepository.findActiveMonsterId(playerId, EncounterStatus.ACTIVE);
    }

    public void requireOutOfCombat(Long playerId, String message) {
        if (isInCombat(playerId)) throw new IllegalStateException(message);
    }
}
