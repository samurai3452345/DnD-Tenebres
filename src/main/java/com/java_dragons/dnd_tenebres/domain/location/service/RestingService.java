package com.java_dragons.dnd_tenebres.domain.location.service;

import com.java_dragons.dnd_tenebres.domain.effect.model.ActiveEffect;
import com.java_dragons.dnd_tenebres.domain.effect.model.EffectType;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationEffect;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationType;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class RestingService {

    @Transactional
    public void takeLongRest(Player player, LocationType type, LocationEffect effect) {
        if (!type.isLongRest()) {
            throw new IllegalArgumentException("Здесь нельзя разбить лагерь!");
        }

        player.healToFull();
        player.clearEffects();

        if (effect == LocationEffect.COZY_TAVERN) {
            if (!player.hasEffect(EffectType.WELL_RESTED)) {
                log.info("Вы отлично отдохнули в уютной таверне. Ваше максимальное ХП временно увеличено!");

                player.addEffect(new ActiveEffect(EffectType.WELL_RESTED, -1, 10));

                player.buffMaxHp(10);
            } else {
                log.info("Вы уже чувствуете себя отлично отдохнувшим, больше ХП не прибавится.");
            }
        }
    }
}