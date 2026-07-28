package com.java_dragons.dnd_tenebres.domain.item.service;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.effect.model.ActiveEffect;
import com.java_dragons.dnd_tenebres.domain.effect.model.EffectType;
import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.item.model.ConsumableAction;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PotionService {

    public boolean applyPotion(ItemTemplate potionTemplate, Player player, List<CombatEvent> events) {

        ConsumableAction action = potionTemplate.getConsumableAction();
        int power = potionTemplate.getStatBudget();

        switch (action) {
            case HEAL_HP -> {
                player.heal(power);
                events.add(new CombatEvent(player.getName(), "USE_POTION_HEAL", player.getName(), power, potionTemplate.getName()));
                return true;
            }
            case RESTORE_MP -> {
                player.restoreMp(power);
                events.add(new CombatEvent(player.getName(), "USE_POTION_MANA", player.getName(), power, potionTemplate.getName()));
                return true;
            }
            case APPLY_REGENERATION -> {
                // А вот здесь зелье вешает временный боевой статус из EffectType
                player.addEffect(new ActiveEffect(EffectType.REGENERATION, 3, power));
                events.add(new CombatEvent(player.getName(), "USE_POTION_BUFF", player.getName(), 0, "Регенерация"));
                return true;
            }
            default -> {
                events.add(new CombatEvent(player.getName(), "USE_POTION_FAIL", player.getName(), 0, "Пустая бутылка или неизвестный эффект"));
                return false;
            }
        }
    }
}