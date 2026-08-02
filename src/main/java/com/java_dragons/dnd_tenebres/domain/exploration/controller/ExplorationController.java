package com.java_dragons.dnd_tenebres.domain.exploration.controller;

import com.java_dragons.dnd_tenebres.domain.exploration.dto.ExplorationReport;
import com.java_dragons.dnd_tenebres.domain.exploration.dto.TravelRequest;
import com.java_dragons.dnd_tenebres.domain.exploration.service.ExplorationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/exploration")
@RequiredArgsConstructor
public class ExplorationController {

    private final ExplorationService explorationService;

    @PostMapping("/{playerId}/hunt")
    public ResponseEntity<ExplorationReport> hunt(@PathVariable Long playerId) {
        ExplorationReport report = explorationService.hunt(playerId);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/{playerId}/search")
    public ResponseEntity<ExplorationReport> search(@PathVariable Long playerId) {
        ExplorationReport report = explorationService.search(playerId);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/{playerId}/travel")
    public ResponseEntity<ExplorationReport> travel(
            @PathVariable Long playerId,
            @RequestBody @Valid TravelRequest request) {
        ExplorationReport report = explorationService.travel(playerId, request.targetLocationId());
        return ResponseEntity.ok(report);
    }
}