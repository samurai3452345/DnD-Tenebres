package com.java_dragons.dnd_tenebres.domain.combat.entity;

import com.java_dragons.dnd_tenebres.domain.combat.model.EncounterReason;
import com.java_dragons.dnd_tenebres.domain.combat.model.EncounterStatus;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "combat_encounters", uniqueConstraints =
        @UniqueConstraint(name = "uq_active_encounter_key", columnNames = "active_player_key"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CombatEncounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version
    private Integer version;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    @Column(name = "player_name", nullable = false, length = 50)
    private String playerName;
    @Column(name = "player_reference_id", nullable = false)
    private Long playerReferenceId;
    @Column(name = "active_player_key")
    private Long activePlayerKey;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EncounterStatus status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EncounterReason reason;
    @Column(nullable = false)
    private int round;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @OneToMany(mappedBy = "encounter", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("turnOrder ASC")
    @Builder.Default
    private List<CombatParticipant> participants = new ArrayList<>();

    public static CombatEncounter start(Player player, EncounterReason reason) {
        return CombatEncounter.builder().player(player).playerName(player.getName()).playerReferenceId(player.getId())
                .activePlayerKey(player.getId()).status(EncounterStatus.ACTIVE).reason(reason)
                .round(1).createdAt(Instant.now()).build();
    }

    public void addParticipant(CombatParticipant participant) {
        participants.add(participant);
    }

    public Optional<CombatParticipant> currentParticipant() {
        return participants.stream().filter(p -> p.getStatus() == com.java_dragons.dnd_tenebres.domain.combat.model.ParticipantStatus.ACTIVE).findFirst();
    }

    public void advanceRound() {
        if (status != EncounterStatus.ACTIVE) throw new IllegalStateException("Encounter is finished");
        round++;
    }

    public void finish(EncounterStatus result) {
        if (status != EncounterStatus.ACTIVE) throw new IllegalStateException("Encounter already finished");
        if (result == EncounterStatus.ACTIVE) throw new IllegalArgumentException("Invalid terminal status");
        status = result;
        activePlayerKey = null;
    }

    @PrePersist
    @PreUpdate
    private void synchronizeHistoricalKeys() {
        if (player != null && playerName == null) playerName = player.getName();
        if (player != null && playerReferenceId == null) playerReferenceId = player.getId();
        activePlayerKey = status == EncounterStatus.ACTIVE && player != null ? player.getId() : null;
    }
}
