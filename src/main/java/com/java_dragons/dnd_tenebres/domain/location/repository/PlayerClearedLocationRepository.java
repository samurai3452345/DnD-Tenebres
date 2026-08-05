package com.java_dragons.dnd_tenebres.domain.location.repository;

import com.java_dragons.dnd_tenebres.domain.location.entity.PlayerClearedLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerClearedLocationRepository extends JpaRepository<PlayerClearedLocation, Long> {

    boolean existsByPlayerIdAndLocationId(Long playerId, String locationId);

    Optional<PlayerClearedLocation> findByPlayerIdAndLocationId(Long playerId, String locationId);
}