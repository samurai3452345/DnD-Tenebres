package com.java_dragons.dnd_tenebres.domain.combat.strategy;

import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Component;

@Component
public class DarkPactStrategy implements ItemPassiveStrategy {

    @Override
    public ItemPassive getTargetPassive() {
        return ItemPassive.DARK_PACT;
    }

    @Override
    public int modifyOutgoingDamage(Player player, Monster monster, int aliveEnemyCount, DamageType damageType, int currentDamage, StringBuilder log) {
        int bonusDamage = (int) (currentDamage * 0.40);
        int finalDamage = currentDamage + bonusDamage;

        log.append(String.format(" Темная магия резонирует, увеличивая урон на 40%% (+%d)! ", bonusDamage));

        return finalDamage;
    }
}