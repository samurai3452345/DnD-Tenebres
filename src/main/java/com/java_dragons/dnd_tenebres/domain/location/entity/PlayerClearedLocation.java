package com.java_dragons.dnd_tenebres.domain.location.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_cleared_locations")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerClearedLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "location_id", nullable = false)
    private String locationId;
}