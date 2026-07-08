package com.java_dragons.dnd_tenebres.domain.location.service;

import com.java_dragons.dnd_tenebres.domain.location.model.LocationEffect;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationType;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestingService {

    @Transactional
    public void takeLongRest(Player player, LocationType type, LocationEffect effect) {
        if (!type.isLongRest()) {
            throw new IllegalArgumentException("Здесь нельзя разбить лагерь!");
        }

        player.healToFull();

        if (effect == LocationEffect.COZY_TAVERN) {
            System.out.println("Вы получили баф: ваше макс хп увеличено!");
            player.buffMaxHp(10);
        }
    }


}
