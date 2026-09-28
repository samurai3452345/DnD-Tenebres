package com.java_dragons.dnd_tenebres.domain.location.controller;

import com.java_dragons.dnd_tenebres.domain.location.dto.LocationResponse;
import com.java_dragons.dnd_tenebres.domain.location.service.LocationService;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/world")
@RequiredArgsConstructor
public class WorldController {
    private final LocationService locationService;

    @GetMapping("/current-location")
    public ResponseEntity<LocationResponse> current(@CurrentPlayerId Long playerId) {
        return ResponseEntity.ok(locationService.getCurrentLocation(playerId));
    }

    @GetMapping("/current-location/connections")
    public ResponseEntity<List<LocationResponse.ConnectionResponse>> connections(@CurrentPlayerId Long playerId) {
        return ResponseEntity.ok(locationService.getCurrentLocation(playerId).connections());
    }
}
