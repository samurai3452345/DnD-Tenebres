package com.java_dragons.dnd_tenebres.infrastructure.audit;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name="audit_entries", indexes=@Index(name="idx_audit_created",columnList="created_at"))
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditEntry {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column private String actor;
 @Column(name="player_id") private Long playerId;
 @Column(nullable=false) private String action;
 @Column(nullable=false) private String outcome;
 @Column(length=2000) private String details;
 @Column(name="created_at",nullable=false) private Instant createdAt;
}
