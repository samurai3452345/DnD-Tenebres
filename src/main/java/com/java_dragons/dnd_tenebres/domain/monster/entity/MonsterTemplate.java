package com.java_dragons.dnd_tenebres.domain.monster.entity;


import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
/*
•	id (BIGSERIAL)
•	name (VARCHAR)
•	biome (VARCHAR) - (например, "FOREST", "DUNGEON")
•	level (INTEGER)
•	base_hp (INTEGER)
•	armor_class (INTEGER)
•	xp_reward (INTEGER)
•	gold_reward (INTEGER)
•	elements (В идеале отдельная таблица связка template_elements, как ты делал для монстров). Напиши SQL-скрипт INSERT, который добавит 3-4 монстров (Гоблин 1 уровня для Леса, Волк 1 уровня для Леса, Скелет 1 уровня для Подземелья).
 */



@Getter
@Entity
@NoArgsConstructor
public class MonsterTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "biome")
    private String biome;

    @Column(name = "level")
    private int level;

    @Column(name = "base_hp")
    private int baseHp;

    @Column(name = "armor_class")
    private int armorClass;

    @Column(name = "xp_reward")
    private int xpReward;

    @Column(name = "gold_reward")
    private int goldReward;


    @ElementCollection(fetch = FetchType.EAGER) // EAGER здесь допустим, так как стихий мало (1-3)
    @CollectionTable(name = "template_elements", joinColumns = @JoinColumn(name = "template_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "element")
    private Set<DamageType> elements = new HashSet<>();
}
