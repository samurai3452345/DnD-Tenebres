package com.java_dragons.dnd_tenebres.domain.location.service;


import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.location.repository.LocationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;

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
}
