package com.java_dragons.dnd_tenebres.domain.player.service;

import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerCreationRequest;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerResponse;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.mapper.PlayerMapper;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerCreationService playerCreationService;
    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;

    @Transactional
    public PlayerResponse createPlayer(PlayerCreationRequest request) {

        Player newPlayer = playerCreationService.createCharacter(request);

        Player savedPlayer = playerRepository.save(newPlayer);

        return playerMapper.toResponse(savedPlayer);
    }

    @Transactional(readOnly = true)
    public PlayerResponse getPlayerById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Игрок с ID " + id + " не найден!"));

        return playerMapper.toResponse(player);
    }
}
