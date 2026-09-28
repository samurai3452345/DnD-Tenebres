package com.java_dragons.dnd_tenebres.domain.player.controller;

import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerResponse;
import com.java_dragons.dnd_tenebres.domain.player.service.PlayerService;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/players")
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping("/me")
    public PlayerResponse getPlayer(@CurrentPlayerId Long id) {
        return playerService.getPlayerById(id);
    }
}
