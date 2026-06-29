package com.java_dragons.dnd_tenebres.domain.item.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Embeddable
public class ItemStats {

    @Column(name = "stat_str", nullable = false)
    private int strength;

    @Column(name = "stat_dex", nullable = false)
    private int dexterity;

    @Column(name = "stat_con", nullable = false)
    private int constitution;

    @Column(name = "stat_int", nullable = false)
    private int intelligence;

    @Column(name = "stat_wis", nullable = false)
    private int wisdom;

    @Column(name = "stat_cha", nullable = false)
    private int charisma;

}
