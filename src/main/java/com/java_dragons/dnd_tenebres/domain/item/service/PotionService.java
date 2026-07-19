package com.java_dragons.dnd_tenebres.domain.item.service;

import com.java_dragons.dnd_tenebres.domain.effect.model.ActiveEffect;
import com.java_dragons.dnd_tenebres.domain.effect.model.EffectType;
import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Service;

@Service
public class PotionService {

    // Принимаем не строку, а сам шаблон предмета
    public boolean applyPotion(ItemTemplate potionTemplate, Player player, StringBuilder log) {
        EffectType effectType = potionTemplate.getPassiveEffect(); // Например, HEAL_INSTANT
        int power = potionTemplate.getStatBudget(); // Допустим, в БД там лежит 50

        switch (effectType) {
            case HEAL_INSTANT -> {
                player.heal(power); // Метод внутри Player, который прибавляет ХП и не дает превысить MaxHP
                log.append(String.format("Вы выпиваете %s и восстанавливаете %d ХП! ", potionTemplate.getName(), power));
                return true;
            }
            case MANA_RESTORE -> {
                // Логика маны
                log.append(String.format("Вы выпиваете %s и чувствуете прилив магических сил! ", potionTemplate.getName()));
                return true;
            }
            case REGENERATION -> {
                // Вешаем бафф на 3 раунда (ActiveEffect)
                player.getActiveEffects().add(new ActiveEffect(EffectType.REGENERATION, 3, power));
                log.append("Мягкое тепло разливается по телу. Активирована регенерация! ");
                return true;
            }
            default -> {
                log.append("Вы выпили непонятную бурду. Ничего не произошло. ");
                return false;
            }
        }
    }
}