package com.java_dragons.dnd_tenebres.domain.location.strategy;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationEffect;
import org.springframework.stereotype.Component;
@Component public class CozyTavernEffectHandler implements LocationEffectHandler {
 public LocationEffect supports(){return LocationEffect.COZY_TAVERN;}
}
