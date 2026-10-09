package com.java_dragons.dnd_tenebres.domain.combat.entity;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "combat_log_entries", indexes = @Index(name = "idx_combat_log_encounter", columnList = "encounter_id,id"))
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CombatLogEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encounter_id", nullable = false)
    private CombatEncounter encounter;
    @Column(nullable = false)
    private int round;
    @Column(nullable = false)
    private String actor;
    @Column(name = "action_type", nullable = false)
    private String actionType;
    @Column(nullable = false)
    private String target;
    @Column(nullable = false)
    private int value;
    @Column(nullable = false, length = 1000)
    private String description;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CombatEvent toEvent() {
        return new CombatEvent(actor, actionType, target, value, description, round);
    }
}
