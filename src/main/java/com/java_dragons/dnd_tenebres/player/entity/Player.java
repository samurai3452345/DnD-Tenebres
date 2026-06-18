package com.java_dragons.dnd_tenebres.player.entity;


import jakarta.persistence.*;
import lombok.Getter;


@Getter
@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "level")
    private int level;

    @Column(name = "experience")
    private long experience;

    @Column(name = "gold")
    private long gold;

    @Column(name = "current_hp")
    private int currentHp;

    @Embedded
    private PlayerStats stats;






}
