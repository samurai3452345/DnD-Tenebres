package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.effect.model.*;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class CombatEffectService {
    public void onRoundStart(Player player, Monster monster, List<CombatEvent> events) {
        for (ActiveEffect e : new ArrayList<>(player.getActiveEffects())) {
            if (e.getType() == EffectType.BLEEDING || e.getType() == EffectType.BURN || e.getType() == EffectType.POISON) {
                int damage = Math.max(1, e.getPower()); player.takeDamage(damage);
                events.add(new CombatEvent(e.getType().name(), "TICK_DAMAGE", player.getName(), damage, e.getType().name()));
            } else if (e.getType() == EffectType.REGENERATION || e.getType() == EffectType.TREATMENT) {
                int before = player.getCurrentHp(); player.heal(Math.max(1, e.getPower()));
                events.add(new CombatEvent(e.getType().name(), "EFFECT_HEAL", player.getName(), player.getCurrentHp()-before, e.getType().name()));
            }
        }
        for (ActiveEffect e : new ArrayList<>(monster.getCombatEffects())) {
            if (e.getType() == EffectType.BLEEDING || e.getType() == EffectType.BURN || e.getType() == EffectType.POISON) {
                int damage = Math.max(1, e.getPower()); monster.takeDamage(damage, com.java_dragons.dnd_tenebres.domain.combat.model.DamageType.PHYSICAL);
                events.add(new CombatEvent(e.getType().name(), "TICK_DAMAGE", monster.getName(), damage, e.getType().name()));
            }
        }
    }

    public boolean blocksAction(Collection<ActiveEffect> effects) {
        return effects.stream().anyMatch(e -> e.getType() == EffectType.STUN || e.getType() == EffectType.BLIND ||
                e.getType() == EffectType.SUPPRESSION || e.getType() == EffectType.FREEZE);
    }

    public int modifyOutgoing(Player player, int damage, List<CombatEvent> events) {
        int result = damage;
        for (ActiveEffect e : player.getActiveEffects()) {
            int before = result;
            if (e.getType() == EffectType.WEAKNESS) result = result * Math.max(0, 100 - Math.max(10, e.getPower())) / 100;
            if (e.getType() == EffectType.DAMAGE_UP || e.getType() == EffectType.BERSERKER_RAGE || e.getType() == EffectType.BLOOD_CONTRACT)
                result = result * (100 + Math.max(10, e.getPower())) / 100;
            if (before != result) events.add(new CombatEvent(e.getType().name(), "DAMAGE_MODIFIER", player.getName(), result-before, e.getType().name()));
        }
        return Math.max(0, result);
    }

    public int modifyIncoming(Player player, int damage, List<CombatEvent> events) {
        int result = damage;
        for (ActiveEffect e : player.getActiveEffects()) {
            int before = result;
            if (e.getType() == EffectType.PROTECTION_REDUCED) result = result * (100 + Math.max(10, e.getPower())) / 100;
            if (e.getType() == EffectType.DAMAGE_REDUCTION) result = result * Math.max(0, 100 - e.getPower()) / 100;
            if (before != result) events.add(new CombatEvent(e.getType().name(), "DEFENSE_MODIFIER", player.getName(), before-result, e.getType().name()));
        }
        return Math.max(0, result);
    }

    public void preventDeath(Player player, List<CombatEvent> events) {
        if (player.getCurrentHp() <= 0 && player.hasEffect(EffectType.SHADOW_DEATH)) {
            player.removeEffect(EffectType.SHADOW_DEATH); player.surviveAtOneHp();
            events.add(new CombatEvent("SHADOW_DEATH", "PREVENT_DEATH", player.getName(), 1, "Смертельный удар предотвращён"));
        }
    }

    public void onRoundEnd(Player player, Monster monster) {
        expire(player.getActiveEffects()); expire(monster.getCombatEffects());
    }

    private void expire(Collection<ActiveEffect> effects) {
        effects.forEach(ActiveEffect::decrementDuration);
        effects.removeIf(e -> e.getDuration() == 0);
    }
}
