package com.java_dragons.dnd_tenebres.domain.location.strategy;
import com.java_dragons.dnd_tenebres.domain.effect.model.*;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationEffect;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Component;
@Component public class AntiMagicEffectHandler implements LocationEffectHandler {
 public LocationEffect supports(){return LocationEffect.ANTI_MAGIC_FIELD;}
 public void onEnter(Player p){p.addEffect(new ActiveEffect(EffectType.MAGIC_SICKNESS,999,50));}
}
