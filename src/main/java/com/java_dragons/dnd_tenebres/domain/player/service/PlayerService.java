package com.java_dragons.dnd_tenebres.domain.player.service;

import com.java_dragons.dnd_tenebres.core.math.ProgressionCalculator;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerCreationRequest;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerResponse;
import com.java_dragons.dnd_tenebres.domain.player.dto.StatAllocationRequest;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.mapper.PlayerMapper;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerCreationService playerCreationService;
    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final ProgressionCalculator progressionCalculator;

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

    @Transactional
    public void addExperienceToPlayer(Player player, long xpGained) {
        player.addExperience(xpGained);

        boolean leveledUp = false;

        while (player.getLevel() < 16 &&
                player.getExperience() >= progressionCalculator.getRequiredXpForLevel(player.getLevel() + 1)) {

            int nextLevel = player.getLevel() + 1;

            int baseHp = progressionCalculator.getHeroBaseHp(nextLevel);

            int baseMp = progressionCalculator.calculateMaxMp(nextLevel, 10);

            player.levelUp(baseHp, baseMp);
            leveledUp = true;
        }

        if (leveledUp) {
            log.info("УРОВЕНЬ ПОВЫШЕН! Вы достигли уровня {}! Начислен 1 свободный поинт.",
                     player.getLevel());
        }
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
}