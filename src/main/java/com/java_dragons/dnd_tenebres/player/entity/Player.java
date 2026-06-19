package com.java_dragons.dnd_tenebres.player.entity;


import jakarta.persistence.*;
import lombok.Getter;


@Getter
@Entity
@Table(name = "players")
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




    public void addExperience(long xp) {
        if(xp < 0){
            throw new  IllegalArgumentException("Опыт не может быть отрицательным!");
        }
        this.experience += xp;
    }

    public void addGold(long amount){
        if(amount < 0){
            throw new IllegalArgumentException("Нельзя добавить отрицательное золото!");
        }

        this.gold += amount;
    }

    public boolean spendGold(long amount){
        if(amount < 0){
            throw new IllegalArgumentException("stupid!");
        }
        if(this.gold < amount){
            return false;
        }
        this.gold -= amount;
        return true;
    }






}
