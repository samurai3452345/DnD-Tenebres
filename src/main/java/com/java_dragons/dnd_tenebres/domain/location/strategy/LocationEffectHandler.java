package com.java_dragons.dnd_tenebres.domain.location.strategy;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationEffect;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
public interface LocationEffectHandler {
    LocationEffect supports();
    default void onEnter(Player player) {}
    default int modifySearchDifficulty(int base) { return base; }
    default int ambushBonusPercent() { return 0; }
}
