package com.java_dragons.dnd_tenebres.domain.player.entity;


import com.java_dragons.dnd_tenebres.domain.effect.model.ActiveEffect;
import com.java_dragons.dnd_tenebres.domain.effect.model.EffectType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

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

    @Builder.Default
    @Column(name = "level")
    private int level = 1;

    @Builder.Default
    @Column(name = "experience")
    private long experience = 0;

    @Builder.Default
    @Column(name = "gold")
    private long gold = 0;

    @Column(name = "current_hp")
    private int currentHp;

    @Column(name = "max_hp", nullable = false)
    private  int maxHp;

    @Embedded
    private PlayerStats stats;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_effects", joinColumns = @JoinColumn(name = "player_id"))
    private Set<ActiveEffect> activeEffects = new HashSet<>();

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

    public void healToFull() {
        this.currentHp = this.maxHp;
    }

    public void buffMaxHp(int percent) {
        int bonus = (this.maxHp * percent) / 100;
        this.maxHp += bonus;
        this.currentHp += bonus;
    }

    public void takeDamage(int damage){
        this.currentHp = Math.max(0, this.currentHp - damage);
    }

    public void addEffect(ActiveEffect effect) {
        this.activeEffects.add(effect);
    }

    public void removeEffect(EffectType type) {
        this.activeEffects.removeIf(e -> e.getType() == type);
    }

    public void clearEffects() {
        this.activeEffects.clear();
    }

    public boolean hasEffect(EffectType type) {
        return this.activeEffects.stream().anyMatch(e -> e.getType() == type);
    }



}
