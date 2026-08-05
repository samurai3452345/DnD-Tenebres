package com.java_dragons.dnd_tenebres.domain.location.service;

import com.java_dragons.dnd_tenebres.domain.location.entity.PlayerClearedLocation;
import com.java_dragons.dnd_tenebres.domain.location.repository.PlayerClearedLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationClearService {

    private final PlayerClearedLocationRepository clearedLocationRepository;

    public boolean isLocationCleared(Long playerId, String locationId) {
        return clearedLocationRepository.existsByPlayerIdAndLocationId(playerId, locationId);
    }

    @Transactional
    public void markLocationAsCleared(Long playerId, String locationId) {
        if (!isLocationCleared(playerId, locationId)) {
            clearedLocationRepository.save(
                    PlayerClearedLocation.builder()
                            .playerId(playerId)
                            .locationId(locationId)
                            .build()
            );
        }
    }
}