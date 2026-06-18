package com.java_dragons.dnd_tenebres.player.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

//Базовые характеристики:

@Embeddable
public class PlayerStats {

    @Column(name = "stat_str")
    private int strength;       //Сила (STR)

    @Column(name = "stat_dex")
    private int dexterity;      //Ловкость (DEX)

    @Column(name = "stat_con")
    private int constitution;   //Телосложение (CON)

    @Column(name = "stat_int")
    private int intelligence;   //Интеллект (INT)

    @Column(name = "stat_wis")
    private int wisdom;         //Мудрость (WIS)

    @Column(name = "stat_cha")
    private int charisma;       //Харизма (CHA)

}
