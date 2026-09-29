package com.java_dragons.dnd_tenebres.domain.combat.repository;

import com.java_dragons.dnd_tenebres.domain.combat.entity.CombatLogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CombatLogEntryRepository extends JpaRepository<CombatLogEntry, Long> {
    List<CombatLogEntry> findTop100ByEncounterIdOrderByIdDesc(Long encounterId);
    Page<CombatLogEntry> findByEncounterIdOrderByIdDesc(Long encounterId, Pageable pageable);
}
