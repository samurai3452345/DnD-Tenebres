package com.java_dragons.dnd_tenebres.domain.player.entity;


import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "level")
    private int level = 1;

    @Column(name = "experience")
    private long experience = 0;

    @Column(name = "gold")
    private long gold = 0;

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
            throw new IllegalArgumentException("Сумма не может быть отрицательной!");
        }
        if(this.gold < amount){
            return false;
        }
        this.gold -= amount;
        return true;
    }






}
