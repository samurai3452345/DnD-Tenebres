package com.java_dragons.dnd_tenebres.infrastructure.audit;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditEntryRepository extends JpaRepository<AuditEntry,Long> {}
