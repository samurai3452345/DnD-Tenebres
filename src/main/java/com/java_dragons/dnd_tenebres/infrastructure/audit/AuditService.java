package com.java_dragons.dnd_tenebres.infrastructure.audit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
@Service @RequiredArgsConstructor
public class AuditService {
 private final AuditEntryRepository repository;
 public void record(String actor, Long playerId, String action, String outcome, String details) {
  repository.save(AuditEntry.builder().actor(actor).playerId(playerId).action(action).outcome(outcome)
    .details(details == null ? "" : details).createdAt(Instant.now()).build());
 }
}
