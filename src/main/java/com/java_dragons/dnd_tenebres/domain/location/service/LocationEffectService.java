package com.java_dragons.dnd_tenebres.domain.location.service;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationEffect;
import com.java_dragons.dnd_tenebres.domain.location.strategy.LocationEffectHandler;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Service
public class LocationEffectService {
 private final Map<LocationEffect,LocationEffectHandler> handlers;
 public LocationEffectService(List<LocationEffectHandler> list){handlers=list.stream().collect(Collectors.toMap(LocationEffectHandler::supports, Function.identity()));}
 public void onEnter(Player p){p.removeEffect(com.java_dragons.dnd_tenebres.domain.effect.model.EffectType.MAGIC_SICKNESS); handler(p).ifPresent(h->h.onEnter(p));}
 public int searchDifficulty(Player p,int base){return handler(p).map(h->h.modifySearchDifficulty(base)).orElse(base);}
 public int ambushBonus(Player p){return handler(p).map(LocationEffectHandler::ambushBonusPercent).orElse(0);}
 private Optional<LocationEffectHandler> handler(Player p){return p.getCurrentLocation()==null?Optional.empty():Optional.ofNullable(handlers.get(p.getCurrentLocation().getEffect()));}
}
