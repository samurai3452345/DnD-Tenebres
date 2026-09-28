package com.java_dragons.dnd_tenebres.domain.combat.entity;

import com.java_dragons.dnd_tenebres.domain.combat.model.ParticipantStatus;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "combat_participants")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CombatParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encounter_id")
    private CombatEncounter encounter;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "monster_id")
    private Monster monster;
    @Column(name = "turn_order", nullable = false)
    private int turnOrder;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status;

    public void activate() {
        status = ParticipantStatus.ACTIVE;
    }

    public void markDead() {
        status = ParticipantStatus.DEAD;
    }

    public void markFled() {
        status = ParticipantStatus.FLED;
    }
}
