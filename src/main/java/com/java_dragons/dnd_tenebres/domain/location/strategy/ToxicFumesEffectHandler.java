package com.java_dragons.dnd_tenebres.domain.location.strategy;
import com.java_dragons.dnd_tenebres.core.random.RandomSource;
import com.java_dragons.dnd_tenebres.domain.effect.model.*;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationEffect;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor public class ToxicFumesEffectHandler implements LocationEffectHandler {
 private final RandomSource random;
 public LocationEffect supports(){return LocationEffect.TOXIC_FUMES;}
 public void onEnter(Player p){if(random.chance(35)) p.addEffect(new ActiveEffect(EffectType.POISON,3,Math.max(1,p.getLevel())));}
 public int modifySearchDifficulty(int base){return base+2;}
}
