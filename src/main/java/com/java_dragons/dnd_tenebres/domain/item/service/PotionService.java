package com.java_dragons.dnd_tenebres.domain.item.service;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.effect.model.ActiveEffect;
import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.java_dragons.dnd_tenebres.domain.effect.model.EffectType.*;

@Service
public class PotionService {

    public boolean applyPotion(ItemTemplate potionTemplate, Player player, List<CombatEvent> events) {
        ItemPassive potionEffect = potionTemplate.getPassiveEffect();
        int power = potionTemplate.getStatBudget();

        switch (potionEffect) {
            case HEAL_INSTANT -> {
                player.heal(power);
                events.add(new CombatEvent(player.getName(), "USE_POTION_HEAL", player.getName(), power, potionTemplate.getName()));
                return true;
            }
            case MANA_RESTORE -> {
                player.restoreMp(power);
                events.add(new CombatEvent(player.getName(), "USE_POTION_MANA", player.getName(), power, potionTemplate.getName()));
                return true;
            }
            case REGENERATION -> {
                player.addEffect(new ActiveEffect(REGENERATION, 3, power));
                events.add(new CombatEvent(player.getName(), "USE_POTION_BUFF", player.getName(), 0, "Регенерация"));
                return true;
            }
            default -> {
                events.add(new CombatEvent(player.getName(), "USE_POTION_FAIL", player.getName(), 0, "Неизвестное зелье"));
                return false;
            }
        }
    }
}