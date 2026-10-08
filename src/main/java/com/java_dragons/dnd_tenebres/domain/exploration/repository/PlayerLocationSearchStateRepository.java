package com.java_dragons.dnd_tenebres.domain.exploration.repository;

import com.java_dragons.dnd_tenebres.domain.exploration.entity.PlayerLocationSearchState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface PlayerLocationSearchStateRepository extends JpaRepository<PlayerLocationSearchState, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PlayerLocationSearchState> findByPlayerIdAndLocationId(Long playerId, String locationId);
}
