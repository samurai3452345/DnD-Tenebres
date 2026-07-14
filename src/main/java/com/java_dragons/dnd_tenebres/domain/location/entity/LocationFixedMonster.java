package com.java_dragons.dnd_tenebres.domain.location.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "location_fixed_monsters")
public class LocationFixedMonster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_id", nullable = false)
    private String locationId;

    @Column(name = "monster_template_name", nullable = false)
    private String monsterTemplateName;

    @Column(name = "count", nullable = false)
    private int count;
}
