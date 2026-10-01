package com.java_dragons.dnd_tenebres.domain.location.strategy;
import com.java_dragons.dnd_tenebres.core.config.GameBalanceProperties;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor public class DarknessEffectHandler implements LocationEffectHandler {
 private final GameBalanceProperties balance;
 public LocationEffect supports(){return LocationEffect.DARKNESS;}
 public int modifySearchDifficulty(int base){return base+5;}
 public int ambushBonusPercent(){return balance.darknessAmbushBonusPercent();}
}
