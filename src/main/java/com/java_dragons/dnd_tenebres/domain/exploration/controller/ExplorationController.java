package com.java_dragons.dnd_tenebres.domain.exploration.controller;

import com.java_dragons.dnd_tenebres.domain.exploration.dto.ExplorationReport;
import com.java_dragons.dnd_tenebres.domain.exploration.dto.TravelRequest;
import com.java_dragons.dnd_tenebres.domain.exploration.service.ExplorationService;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/exploration")
@RequiredArgsConstructor
public class ExplorationController {

    private final ExplorationService explorationService;

    @PostMapping("/hunt")
    public ResponseEntity<ExplorationReport> hunt(@CurrentPlayerId Long playerId) {
        ExplorationReport report = explorationService.hunt(playerId);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/search")
    public ResponseEntity<ExplorationReport> search(@CurrentPlayerId Long playerId) {
        ExplorationReport report = explorationService.search(playerId);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/travel")
    public ResponseEntity<ExplorationReport> travel(
            @CurrentPlayerId Long playerId,
            @RequestBody @Valid TravelRequest request) {
        ExplorationReport report = explorationService.travel(playerId, request.targetLocationId());
        return ResponseEntity.ok(report);
    }
}