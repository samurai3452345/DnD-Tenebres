package com.java_dragons.dnd_tenebres.domain.player.controller;

import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerCreationRequest;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerResponse;
import com.java_dragons.dnd_tenebres.domain.player.dto.CharacterSummaryResponse;
import com.java_dragons.dnd_tenebres.domain.player.service.PlayerService;
import com.java_dragons.dnd_tenebres.infrastructure.security.dto.AuthResponse;
import com.java_dragons.dnd_tenebres.infrastructure.security.repository.UserAccountRepository;
import com.java_dragons.dnd_tenebres.infrastructure.security.service.AuthService;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/players")
public class PlayerController {

    private final PlayerService playerService;
    private final UserAccountRepository userAccountRepository;
    private final AuthService authService;

    @PostMapping
    @Transactional
    public AuthResponse createPlayer(
            Authentication authentication,
            @Valid @RequestBody PlayerCreationRequest request
    ) {
        var account = userAccountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт не найден"));

        playerService.createPlayer(account.getId(), request);
        return authService.createResponse(account);
    }

    @GetMapping
    public List<CharacterSummaryResponse> getCharacters(Authentication authentication) {
        var account = userAccountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт не найден"));
        return playerService.getCharacters(account.getId());
    }

    @PostMapping("/{playerId}/select")
    public AuthResponse selectCharacter(Authentication authentication, @PathVariable Long playerId) {
        var account = userAccountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт не найден"));
        playerService.verifyCharacterOwnership(account.getId(), playerId);
        return authService.createResponse(account, playerId);
    }

    @GetMapping("/me")
    public PlayerResponse getPlayer(@CurrentPlayerId Long id) {
        return playerService.getPlayerById(id);
    }
}
