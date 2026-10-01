package com.java_dragons.dnd_tenebres.domain.player.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "player_story_flags", uniqueConstraints = @UniqueConstraint(name = "uq_player_story_flag", columnNames = {"player_id", "flag_code"}))
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class PlayerStoryFlag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "player_id", nullable = false)
    private Long playerId;
    @Column(name = "flag_code", nullable = false)
    private String flagCode;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
