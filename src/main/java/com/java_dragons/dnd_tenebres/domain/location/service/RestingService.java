package com.java_dragons.dnd_tenebres.domain.location.service;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.domain.effect.model.ActiveEffect;
import com.java_dragons.dnd_tenebres.domain.effect.model.EffectCategory;
import com.java_dragons.dnd_tenebres.domain.effect.model.EffectType;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationEffect;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationType;
import com.java_dragons.dnd_tenebres.domain.player.dto.RestReport;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestingService {

    private final PlayerRepository playerRepository;

    private static final int TAVERN_COST = 50;

    @Transactional
    public RestReport takeShortRest(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));

        if (player.isInCombat()) {
            throw new IllegalStateException("Вы не можете разбить привал во время боя!");
        }

        LocationType locType = player.getCurrentLocation().getType();

        if (!player.consumeItemByName("Припасы")) {
            throw new IllegalStateException("Для привала нужны 'Припасы'!");
        }

        player.removeEffect(EffectType.WELL_RESTED);

        int d20 = DiceRoller.rollD20();
        boolean isAmbushed = false;

        if (locType == LocationType.NEUTRAL && d20 <= 7) {
            isAmbushed = true;
        } else if (locType == LocationType.DANGEROUS && d20 <= 15) {
            isAmbushed = true;
        }

        if (isAmbushed) {
            log.warn("Отдых прерван! Засада!");

            // Берем ID локации вместо биома и уровня
            String locationId = player.getCurrentLocation().getId();

            return new RestReport("Ваш отдых был прерван внезапным нападением!", true, locationId);
        }

        player.heal(player.getMaxHp() / 2);
        player.restoreMp(player.getMaxMp() / 2);
        player.removeEffect(EffectType.POISON);
        player.removeEffect(EffectType.BLEEDING);
        player.removeEffect(EffectType.BURN);

        return new RestReport("Вы немного отдохнули и перевели дух.", false);
    }

    @Transactional
    public RestReport takeLongRest(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));

        if (player.isInCombat()) {
            throw new IllegalStateException("Вы не можете путешествовать, пока находитесь в бою!");
        }

        LocationType type = player.getCurrentLocation().getType();
        LocationEffect effect = player.getCurrentLocation().getEffect();

        if (!type.isLongRest()) {
            throw new IllegalArgumentException("Здесь нельзя безопасно переночевать!");
        }

        if (!player.spendGold(TAVERN_COST)) {
            throw new IllegalArgumentException("Недостаточно золота для ночевки!");
        }

        player.healToFull();
        player.restoreMp(player.getMaxMp());

        player.getActiveEffects().removeIf(e -> e.getType().getEffectCategory() == EffectCategory.DEBUFF);

        if (effect == LocationEffect.COZY_TAVERN) {
            player.removeEffect(EffectType.WELL_RESTED);
            player.addEffect(new ActiveEffect(EffectType.WELL_RESTED, 999, 10));
            log.info("Вы отлично отдохнули в уютной таверне. Наложен бафф WELL_RESTED.");
        }

        return new RestReport("Вы отлично выспались, раны затянулись, а мана восстановлена.", false);
    }
}