package com.java_dragons.dnd_tenebres.domain.player.controller;

import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerCreationRequest;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerResponse;
import com.java_dragons.dnd_tenebres.domain.player.mapper.PlayerMapper;
import com.java_dragons.dnd_tenebres.domain.player.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/players")
public class PlayerController {
    private final PlayerService playerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Автоматически вернет статус 201 Created при успехе
    public PlayerResponse createPlayer(@RequestBody @Valid PlayerCreationRequest request) {
        return playerService.createPlayer(request);
    }

    @GetMapping("/{id}")
    public PlayerResponse getPlayer(@PathVariable long id) {
        return playerService.getPlayerById(id);
    }

}
