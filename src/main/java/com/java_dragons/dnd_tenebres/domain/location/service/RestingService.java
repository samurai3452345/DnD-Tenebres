package com.java_dragons.dnd_tenebres.domain.location.service;

import com.java_dragons.dnd_tenebres.core.random.RandomSource;
import com.java_dragons.dnd_tenebres.core.config.GameBalanceProperties;
import com.java_dragons.dnd_tenebres.domain.economy.service.WalletService;
import com.java_dragons.dnd_tenebres.domain.economy.model.WalletReason;
import com.java_dragons.dnd_tenebres.domain.monster.service.MonsterSpawnerService;
import com.java_dragons.dnd_tenebres.domain.combat.service.CombatEncounterService;
import com.java_dragons.dnd_tenebres.domain.combat.model.EncounterReason;
import com.java_dragons.dnd_tenebres.domain.effect.model.ActiveEffect;
import com.java_dragons.dnd_tenebres.domain.effect.model.EffectCategory;
import com.java_dragons.dnd_tenebres.domain.effect.model.EffectType;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationEffect;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationType;
import com.java_dragons.dnd_tenebres.domain.player.dto.RestReport;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import com.java_dragons.dnd_tenebres.domain.exploration.service.SearchAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestingService {

    private final PlayerRepository playerRepository;
    private final RandomSource randomSource;
    private final GameBalanceProperties balance;
    private final WalletService walletService;
    private final MonsterSpawnerService monsterSpawnerService;
    private final CombatEncounterService encounterService;
    private final LocationEffectService locationEffectService;
    private final SearchAttemptService searchAttemptService;

    @Transactional
    public RestReport takeShortRest(Long playerId) {
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));

        if (player.isInCombat()) {
            throw new IllegalStateException("Вы не можете разбить привал во время боя!");
        }

        if (player.getCurrentLocation() == null) throw new IllegalStateException("У игрока нет текущей локации");
        LocationType locType = player.getCurrentLocation().getType();
        LocationEffect locationEffect = player.getCurrentLocation().getEffect();

        if (!player.canTakeShortRest()) throw new IllegalStateException("Короткий отдых уже использован в это посещение");
        if (!player.consumeItemByName("Припасы")) {
            throw new IllegalStateException("Для привала нужны 'Припасы'!");
        }

        player.removeEffect(EffectType.WELL_RESTED);

        player.markShortRestUsed();
        int chance = locType == LocationType.DANGEROUS ? balance.shortRestDangerousAmbushPercent()
                : locType == LocationType.NEUTRAL ? balance.shortRestNeutralAmbushPercent() : 0;
        chance = Math.min(100, chance + locationEffectService.ambushBonus(player));
        boolean isAmbushed = randomSource.chance(chance);

        if (isAmbushed) {
            log.warn("Отдых прерван! Засада!");

            // Берем ID локации вместо биома и уровня
            String locationId = player.getCurrentLocation().getId();

            var monster = monsterSpawnerService.spawnRandomMonster(locationId);
            encounterService.startEncounter(playerId, java.util.List.of(monster), EncounterReason.REST_AMBUSH);
            var state = encounterService.applyAmbushOpening(playerId, monster);
            return new RestReport("Ваш отдых был прерван внезапным нападением!", true, locationId, state);
        }

        if (locationEffect != LocationEffect.TOXIC_FUMES) player.heal(player.getMaxHp() / 2);
        player.restoreMp(player.getMaxMp() / 2);
        player.removeEffect(EffectType.POISON);
        player.removeEffect(EffectType.BLEEDING);
        player.removeEffect(EffectType.BURN);
        int searchAttempts = searchAttemptService.restore(
                playerId, player.getCurrentLocation().getId(), 4);

        String message = locationEffect == LocationEffect.TOXIC_FUMES
                ? "Ядовитые испарения не дали восстановить здоровье. Попыток поиска: " + searchAttempts + "."
                : "Вы немного отдохнули и перевели дух. Попыток поиска: " + searchAttempts + ".";
        return new RestReport(message, false);
    }

    @Transactional
    public RestReport takeLongRest(Long playerId) {
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));

        if (player.isInCombat()) {
            throw new IllegalStateException("Вы не можете путешествовать, пока находитесь в бою!");
        }

        if (player.getCurrentLocation() == null) {
            throw new IllegalStateException("У игрока нет текущей локации");
        }
        LocationEffect effect = player.getCurrentLocation().getEffect();

        if (effect != LocationEffect.COZY_TAVERN) {
            throw new IllegalArgumentException("Долгий отдых доступен только в таверне");
        }

        walletService.debit(player, balance.tavernCost(), WalletReason.REST, "LOCATION", player.getCurrentLocation().getId());

        player.getActiveEffects().removeIf(e -> e.getType().getEffectCategory() == EffectCategory.DEBUFF);

        player.removeEffect(EffectType.WELL_RESTED);
        player.addEffect(new ActiveEffect(EffectType.WELL_RESTED, 999, 10));
        player.healToFull();
        player.restoreMpToFull();
        log.info("Вы отлично отдохнули в уютной таверне. Наложен бафф WELL_RESTED.");

        return new RestReport("Вы отлично выспались, раны затянулись, а мана восстановлена.", false);
    }
}
