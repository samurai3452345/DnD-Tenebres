package com.java_dragons.dnd_tenebres.domain.location.controller;

import com.java_dragons.dnd_tenebres.domain.location.service.RestingService;
import com.java_dragons.dnd_tenebres.domain.player.dto.RestReport;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/rest") @RequiredArgsConstructor
public class RestingController {
    private final RestingService restingService;
    @PostMapping("/short") public ResponseEntity<RestReport> shortRest(@CurrentPlayerId Long playerId) {
        return ResponseEntity.ok(restingService.takeShortRest(playerId));
    }
    @PostMapping("/long") public ResponseEntity<RestReport> longRest(@CurrentPlayerId Long playerId) {
        return ResponseEntity.ok(restingService.takeLongRest(playerId));
    }
}
