package com.java_dragons.dnd_tenebres.domain.combat.repository;

import com.java_dragons.dnd_tenebres.domain.combat.entity.CombatEncounter;
import com.java_dragons.dnd_tenebres.domain.combat.model.EncounterStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface CombatEncounterRepository extends JpaRepository<CombatEncounter, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from CombatEncounter e where e.player.id=:playerId and e.status=:status")
    Optional<CombatEncounter> findForUpdate(@Param("playerId") Long playerId, @Param("status") EncounterStatus status);

    @Query("select distinct e from CombatEncounter e left join fetch e.participants p left join fetch p.monster where e.player.id=:playerId and e.status=:status")
    Optional<CombatEncounter> findCurrent(@Param("playerId") Long playerId, @Param("status") EncounterStatus status);

    Optional<CombatEncounter> findByIdAndPlayerId(Long id, Long playerId);

    Optional<CombatEncounter> findFirstByPlayerIdOrderByCreatedAtDesc(Long playerId);
    long countByStatus(EncounterStatus status);
}
