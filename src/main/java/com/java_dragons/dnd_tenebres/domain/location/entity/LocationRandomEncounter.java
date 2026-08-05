package com.java_dragons.dnd_tenebres.domain.location.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "location_random_encounters")
public class LocationRandomEncounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_id", nullable = false)
    private String locationId;

    @Column(name = "monster_template_name", nullable = false)
    private String monsterTemplateName;

    @Column(name = "spawn_chance", nullable = false)
    private int spawnChance;
}