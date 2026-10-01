package com.java_dragons.dnd_tenebres.infrastructure.metrics;
import com.java_dragons.dnd_tenebres.domain.combat.model.EncounterStatus;
import com.java_dragons.dnd_tenebres.domain.combat.repository.CombatEncounterRepository;
import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;
@Component
public class GameMetrics {
 private final Counter registrations;
 private final Counter contentErrors;
 public GameMetrics(MeterRegistry registry, CombatEncounterRepository encounters) {
  registrations=registry.counter("game.registrations"); contentErrors=registry.counter("game.content.errors");
  Gauge.builder("game.combat.active", encounters, r->r.countByStatus(EncounterStatus.ACTIVE)).register(registry);
 }
 public void registration(){registrations.increment();}
 public void contentError(){contentErrors.increment();}
}
