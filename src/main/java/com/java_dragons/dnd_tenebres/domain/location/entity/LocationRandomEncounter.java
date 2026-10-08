package com.java_dragons.dnd_tenebres.domain.location.entity;

import com.java_dragons.dnd_tenebres.domain.monster.entity.MonsterTemplate;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monster_template_id", nullable = false)
    private MonsterTemplate monsterTemplate;

    @Column(name = "spawn_chance", nullable = false)
    private int spawnChance;

    public String getMonsterTemplateName() {
        return monsterTemplate.getName();
    }
}
