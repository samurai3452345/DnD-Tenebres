package com.java_dragons.dnd_tenebres.player.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

//Базовые характеристики:

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Embeddable
public class PlayerStats {

    @Column(name = "stat_str", nullable = false)
    private int strength;       //Сила (STR)

    @Column(name = "stat_dex", nullable = false)
    private int dexterity;      //Ловкость (DEX)

    @Column(name = "stat_con", nullable = false)
    private int constitution;   //Телосложение (CON)

    @Column(name = "stat_int", nullable = false)
    private int intelligence;   //Интеллект (INT)

    @Column(name = "stat_wis", nullable = false)
    private int wisdom;         //Мудрость (WIS)

    @Column(name = "stat_cha", nullable = false)
    private int charisma;       //Харизма (CHA)

}
