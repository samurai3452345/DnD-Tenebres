package com.java_dragons.dnd_tenebres.domain.exploration.service;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.core.math.StatMathUtils;
import com.java_dragons.dnd_tenebres.domain.exploration.dto.ExplorationReport;
import com.java_dragons.dnd_tenebres.domain.item.service.InventoryService;
import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.location.entity.LocationLootEntry;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationType;
import com.java_dragons.dnd_tenebres.domain.location.repository.LocationFixedMonsterRepository;
import com.java_dragons.dnd_tenebres.domain.location.repository.LocationLootEntryRepository;
import com.java_dragons.dnd_tenebres.domain.location.service.LocationClearService;
import com.java_dragons.dnd_tenebres.domain.location.service.LocationService;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.service.MonsterSpawnerService;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import com.java_dragons.dnd_tenebres.domain.combat.service.CombatEncounterService;
import com.java_dragons.dnd_tenebres.domain.combat.model.EncounterReason;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.context.ApplicationEventPublisher;
import com.java_dragons.dnd_tenebres.core.event.LocationVisitedEvent;
import com.java_dragons.dnd_tenebres.core.event.ItemLootedEvent;
import com.java_dragons.dnd_tenebres.core.random.RandomSource;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExplorationService {

    private final MonsterSpawnerService monsterSpawnerService;
    private final LocationService locationService;
    private final InventoryService inventoryService;
    private final PlayerRepository playerRepository;
    private final LocationClearService locationClearService;
    private final LocationFixedMonsterRepository fixedMonsterRepository;
    private final LocationLootEntryRepository locationLootEntryRepository;
    private final CombatEncounterService combatEncounterService;
    private final com.java_dragons.dnd_tenebres.domain.location.service.LocationUnlockService locationUnlockService;
    private final ApplicationEventPublisher eventPublisher;
    private final RandomSource randomSource;
    private final com.java_dragons.dnd_tenebres.domain.location.service.LocationEffectService locationEffectService;
    private final SearchAttemptService searchAttemptService;

    @Transactional
    public ExplorationReport travel(Long playerId, String targetLocationId) {
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));
        Location currentLocation = player.getCurrentLocation();

        if (player.isInCombat()) {
            throw new IllegalStateException("Вы не можете путешествовать, пока находитесь в бою!");
        }

        Set<Location> paths = locationService.getAvailableConnections(currentLocation.getId());

        Location targetLocation = paths.stream()
                .filter(loc -> loc.getId().equals(targetLocationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Вы не можете попасть туда отсюда!"));
        locationUnlockService.requireAccess(player, currentLocation, targetLocation);

        player.moveTo(targetLocation);
        locationEffectService.onEnter(player);
        playerRepository.save(player);
        eventPublisher.publishEvent(new LocationVisitedEvent(playerId, targetLocation.getId()));
        log.info("Игрок {} перешел в локацию {}", player.getName(), targetLocation.getName());

        if (targetLocation.getType() == LocationType.DANGEROUS) {
            boolean isCleared = locationClearService.isLocationCleared(playerId, targetLocation.getId());

            if (!isCleared) {
                boolean hasEnemies = fixedMonsterRepository.existsByLocationId(targetLocation.getId());

                if (hasEnemies) {
                    List<Monster> squad = monsterSpawnerService.spawnFixedMonstersForLocation(targetLocation.getId());

                    combatEncounterService.startEncounter(playerId, squad, EncounterReason.TRAVEL);

                    log.info("Засада в локации {}! Врагов: {}", targetLocation.getName(), squad.size());

                    return ExplorationReport.combat("Как только вы вошли, на вас напали!", squad.get(0).getId());
                } else {
                    locationClearService.markLocationAsCleared(playerId, targetLocation.getId());
                }
            }
        }
        return ExplorationReport.moved(targetLocation.getName());
    }

    @Transactional
    public ExplorationReport hunt(Long playerId) {
        Player player = getPlayer(playerId);

        if (player.isInCombat()) {
            throw new IllegalStateException("Вы уже в бою! Сначала победите текущего врага.");
        }

        Location location = player.getCurrentLocation();

        if (location.getType() == LocationType.SAFE_ZONE) {
            return ExplorationReport.nothing("Здесь безопасно. Вы не нашли никого для охоты.");
        }

        if (location.getType() == LocationType.DANGEROUS) {
            return ExplorationReport.nothing("Вы победили всех врагов, здесь пусто.");
        }

        int roll = DiceRoller.rollD20();
        int wisMod = StatMathUtils.calculateModifier(player.getStats().getWisdom());
        int totalCheck = roll + wisMod;

        int difficultyClass = location.getHuntDifficulty();

        if (totalCheck >= difficultyClass) {
            Monster monster = monsterSpawnerService.spawnRandomMonster(location.getId());

            combatEncounterService.startEncounter(playerId, List.of(monster), EncounterReason.HUNT);

            log.info("Игрок {} нашел монстра: {}", player.getName(), monster.getName());
            return ExplorationReport.combat("Из теней появляется " + monster.getName() + "!", monster.getId());
        }

        return ExplorationReport.nothing("Вы долго бродили по окрестностям, но так никого и не выследили.");
    }

    @Transactional
    public ExplorationReport search(Long playerId) {
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));

        if (player.isInCombat()) {
            throw new IllegalStateException("Нельзя искать предметы, когда вас пытаются убить!");
        }

        Location location = player.getCurrentLocation();

        int remainingAttempts = searchAttemptService.consume(playerId, location.getId());

        // 1. Бросаем кубик на успешность обыска
        int roll = DiceRoller.rollD20();
        int searchDifficulty = locationEffectService.searchDifficulty(player, location.getSearchDifficulty());
        if (roll < searchDifficulty) {
            return ExplorationReport.nothing("Вы ничего не нашли (Провал проверки обыска: " + roll + " < "
                    + searchDifficulty + "). Осталось попыток: " + remainingAttempts + '.');
        }

        List<LocationLootEntry> lootTable = locationLootEntryRepository.findByLocationId(location.getId());

        if (lootTable.isEmpty()) {
            return ExplorationReport.nothing("Здесь абсолютно нечего искать. Осталось попыток: " + remainingAttempts + '.');
        }

        List<String> foundItemsList = new ArrayList<>();
        StringBuilder foundItemsMsg = new StringBuilder("Вы нашли: ");

        for (LocationLootEntry entry : lootTable) {
            if (DiceRoller.rollD100() <= entry.getFindChance()) {
                if (entry.getMinAmount() <= 0 || entry.getMaxAmount() < entry.getMinAmount()) {
                    throw new IllegalStateException("Некорректный диапазон добычи в локации " + location.getId());
                }
                int amount = randomSource.nextInt(entry.getMinAmount(), Math.addExact(entry.getMaxAmount(), 1));

                inventoryService.addItemToPlayer(player, entry.getItemTemplate().getName(), amount);
                eventPublisher.publishEvent(new ItemLootedEvent(
                        playerId,
                        entry.getItemTemplate().getName(),
                        amount
                ));

                String itemDisplay = entry.getItemTemplate().getName() + " (x" + amount + ")";
                foundItemsList.add(itemDisplay);
                foundItemsMsg.append(itemDisplay).append(", ");
            }
        }

        if (foundItemsList.isEmpty()) {
            return ExplorationReport.nothing("Вы тщательно осмотрели местность, но ничего интересного не попалось. Осталось попыток: "
                    + remainingAttempts + '.');
        }

        foundItemsMsg.setLength(foundItemsMsg.length() - 2);

        foundItemsMsg.append(". Осталось попыток: ").append(remainingAttempts).append('.');
        return ExplorationReport.loot(foundItemsMsg.toString(), foundItemsList);
    }

    private Player getPlayer(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));
    }
}
