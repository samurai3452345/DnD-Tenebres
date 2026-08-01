package com.java_dragons.dnd_tenebres.domain.player.controller;

import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerResponse;
import com.java_dragons.dnd_tenebres.domain.player.dto.StatAllocationRequest;
import com.java_dragons.dnd_tenebres.domain.player.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/players/{playerId}/stats")
@RequiredArgsConstructor
public class PlayerStatsController {

    private final PlayerService playerService;

    @PostMapping("/allocate")
    public PlayerResponse allocateStats(
            @PathVariable Long playerId,
            @RequestBody @Valid StatAllocationRequest request) {

        return playerService.allocateStats(playerId, request);
    }
}