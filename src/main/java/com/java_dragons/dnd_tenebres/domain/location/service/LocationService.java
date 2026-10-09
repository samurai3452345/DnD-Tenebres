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
    private final LocationUnlockService unlockService;
    private final com.java_dragons.dnd_tenebres.domain.location.repository.LocationLootEntryRepository lootRepository;

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
        var player = playerRepository.findById(playerId).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Игрок не найден"));
        Location location = locationRepository.findByIdWithConnections(player.getCurrentLocation().getId())
                .orElseThrow(() -> new EntityNotFoundException("Current location is missing"));
        boolean cleared = clearedLocationRepository.existsByPlayerIdAndLocationId(playerId, location.getId());
        var loot = lootRepository.findByLocationId(location.getId());
        List<String> actions;
        if (location.getType() == LocationType.SAFE_ZONE) actions = List.of("TRAVEL", "LONG_REST");
        else if (loot.isEmpty()) actions = List.of("TRAVEL", "HUNT", "SHORT_REST");
        else actions = List.of("TRAVEL", "HUNT", "SEARCH", "SHORT_REST");
        var connections = location.getConnectedLocations().stream().map(target -> {
            var access = unlockService.check(player, location, target);
            return new LocationResponse.ConnectionResponse(target.getId(), target.getName(), target.getLevel(),
                    target.getType(), target.isBossRoom(), access.open(), access.blockedReasons());
        }).toList();
        var resources = loot.stream().map(entry -> new LocationResponse.ResourceResponse(
                entry.getItemTemplate().getId(), entry.getItemTemplate().getName(), entry.getMinAmount(),
                entry.getMaxAmount(), entry.getFindChance())).toList();
        return new LocationResponse(location.getId(), location.getName(), location.getZoneName(), location.getDescription(), location.getType(),
                location.getBiome(), location.getLevel(), location.getEffect(), cleared, location.isBossRoom(),
                actions, connections, resources);
    }
}
