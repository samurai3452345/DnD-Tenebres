package com.java_dragons.dnd_tenebres.domain.location.service;


import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.location.repository.LocationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.List;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import com.java_dragons.dnd_tenebres.domain.location.repository.PlayerClearedLocationRepository;
import com.java_dragons.dnd_tenebres.domain.location.dto.LocationResponse;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationType;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final PlayerRepository playerRepository;
    private final PlayerClearedLocationRepository clearedLocationRepository;

    @Transactional(readOnly = true)
    public Location getLocationById(String id){
        return locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Локация с ID" + id + " не найдена"));
    }

    @Transactional(readOnly = true)
    public Set<Location> getAvailableConnections(String locationId){
        Location location = getLocationById(locationId);

        location.getConnectedLocations().size();

        return location.getConnectedLocations();
    }

    @Transactional(readOnly = true)
    public LocationResponse getCurrentLocation(Long playerId) {
        var player = playerRepository.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Player not found"));
        Location location = locationRepository.findByIdWithConnections(player.getCurrentLocation().getId())
                .orElseThrow(() -> new EntityNotFoundException("Current location is missing"));
        boolean cleared = clearedLocationRepository.existsByPlayerIdAndLocationId(playerId, location.getId());
        List<String> actions = location.getType() == LocationType.SAFE_ZONE
                ? List.of("TRAVEL", "LONG_REST") : List.of("TRAVEL", "HUNT", "SEARCH", "SHORT_REST");
        var connections = location.getConnectedLocations().stream().map(target -> {
            boolean open = player.getLevel() >= target.getLevel();
            return new LocationResponse.ConnectionResponse(target.getId(), target.getName(), target.getLevel(), open,
                    open ? null : "Требуется уровень " + target.getLevel());
        }).toList();
        return new LocationResponse(location.getId(), location.getName(), location.getName(), location.getType(),
                location.getBiome(), location.getLevel(), location.getEffect(), cleared, actions, connections);
    }
}
