package com.java_dragons.dnd_tenebres.domain.player.service;

import com.java_dragons.dnd_tenebres.core.math.ProgressionCalculator;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerCreationRequest;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerResponse;
import com.java_dragons.dnd_tenebres.domain.player.dto.StatAllocationRequest;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.mapper.PlayerMapper;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.location.service.LocationService;
import com.java_dragons.dnd_tenebres.domain.item.service.InventoryService;
import com.java_dragons.dnd_tenebres.domain.player.dto.LevelUpResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.java_dragons.dnd_tenebres.core.config.GamePlayerProperties;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerCreationService playerCreationService;
    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final ProgressionCalculator progressionCalculator;
    private final LocationService locationService;
    private final InventoryService inventoryService;
    private final PlayerProgressionService progressionService;
    private final GamePlayerProperties gamePlayerProperties;
    private final com.java_dragons.dnd_tenebres.domain.economy.service.WalletService walletService;


    @Transactional
    public PlayerResponse createPlayer(PlayerCreationRequest request) {
        Player newPlayer = playerCreationService.createCharacter(request);
        newPlayer.moveTo(locationService.getLocationById(gamePlayerProperties.getStartLocationId()));
        Player savedPlayer = playerRepository.save(newPlayer);
        walletService.credit(savedPlayer, gamePlayerProperties.getStartGold(),
                com.java_dragons.dnd_tenebres.domain.economy.model.WalletReason.STARTING_GOLD,
                "PLAYER", savedPlayer.getId().toString());
        gamePlayerProperties.getStartItems().forEach(item ->
                inventoryService.addItemToPlayer(savedPlayer, item.getTemplate(), item.getAmount()));
        return playerMapper.toResponse(savedPlayer);
    }

    @Transactional(readOnly = true)
    public PlayerResponse getPlayerById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Игрок с ID " + id + " не найден!"));
        return playerMapper.toResponse(player);
    }

    @Transactional
    public LevelUpResult addExperienceToPlayer(Player player, long xpGained) {
        LevelUpResult result = progressionService.grantExperience(player, xpGained);
        if (result.leveledUp()) {
            log.info("УРОВЕНЬ ПОВЫШЕН! Вы достигли уровня {}! Начислен 1 свободный поинт.",
                     player.getLevel());
        }
        return result;
    }

    @Transactional
    public PlayerResponse allocateStats(Long playerId, StatAllocationRequest request) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));

        player.allocateStats(
                request.addStrength(), request.addDexterity(), request.addConstitution(),
                request.addIntelligence(), request.addWisdom(), request.addCharisma()
        );

        return playerMapper.toResponse(player);
    }

    @Transactional
    public void respawnPlayer(Player player) {
        player.revive();
        player.clearEffects();

        Location tavern = locationService.getLocationById("city_tavern");
        player.moveTo(tavern);
    }

}
