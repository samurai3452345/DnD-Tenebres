package com.java_dragons.dnd_tenebres.domain.player.service;

import com.java_dragons.dnd_tenebres.core.math.ProgressionCalculator;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerCreationRequest;
import com.java_dragons.dnd_tenebres.domain.player.dto.CharacterSummaryResponse;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerResponse;
import com.java_dragons.dnd_tenebres.domain.player.dto.StatAllocationRequest;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.mapper.PlayerMapper;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.location.service.LocationService;
import com.java_dragons.dnd_tenebres.domain.item.service.InventoryService;
import com.java_dragons.dnd_tenebres.domain.player.dto.LevelUpResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.java_dragons.dnd_tenebres.core.config.GamePlayerProperties;

import java.util.List;

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
    private final com.java_dragons.dnd_tenebres.domain.combat.service.CombatStateService combatStateService;


    @Transactional
    public PlayerResponse createPlayer(Long accountId, PlayerCreationRequest request) {
        Player newPlayer = playerCreationService.createCharacter(accountId, request);
        newPlayer.moveTo(locationService.getLocationById(gamePlayerProperties.getStartLocationId()));
        Player savedPlayer = playerRepository.save(newPlayer);
        walletService.credit(savedPlayer, gamePlayerProperties.getStartGold(),
                com.java_dragons.dnd_tenebres.domain.economy.model.WalletReason.STARTING_GOLD,
                "PLAYER", savedPlayer.getId().toString());
        gamePlayerProperties.getStartItems().forEach(item ->
                inventoryService.addItemToPlayer(savedPlayer, item.getTemplate(), item.getAmount()));
        return toResponse(savedPlayer);
    }

    @Transactional(readOnly = true)
    public List<CharacterSummaryResponse> getCharacters(Long accountId) {
        return playerRepository.findAllByAccountIdOrderByIdAsc(accountId).stream()
                .map(player -> new CharacterSummaryResponse(
                        player.getId(),
                        player.getName(),
                        player.getLevel(),
                        player.getCurrentLocation() == null ? null : player.getCurrentLocation().getId(),
                        player.getCurrentLocation() == null ? null : player.getCurrentLocation().getName(),
                        player.getGold(),
                        player.getStats(),
                        player.getStatPoints()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public void verifyCharacterOwnership(Long accountId, Long playerId) {
        if (!playerRepository.existsByIdAndAccountId(playerId, accountId)) {
            throw new EntityNotFoundException("Персонаж не найден в этом аккаунте");
        }
    }

    @Transactional
    public void deleteCharacter(Long accountId, Long playerId) {
        Player player = playerRepository.findByIdAndAccountId(playerId, accountId)
                .orElseThrow(() -> new EntityNotFoundException("Персонаж не найден в этом аккаунте"));
        combatStateService.requireOutOfCombat(playerId, "Нельзя удалить персонажа во время активного боя");
        playerRepository.delete(player);
    }

    @Transactional(readOnly = true)
    public PlayerResponse getPlayerById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Игрок не найден"));
        return toResponse(player);
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
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Игрок не найден"));

        combatStateService.requireOutOfCombat(playerId, "Нельзя распределять характеристики во время боя");

        player.allocateStats(
                request.addStrength(), request.addDexterity(), request.addConstitution(),
                request.addIntelligence(), request.addWisdom(), request.addCharisma()
        );

        return toResponse(player);
    }

    private PlayerResponse toResponse(Player player) {
        return playerMapper.toResponse(player, combatStateService.activeMonsterId(player.getId()).orElse(null));
    }

}
