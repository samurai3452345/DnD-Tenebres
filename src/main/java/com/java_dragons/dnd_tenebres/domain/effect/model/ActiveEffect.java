package com.java_dragons.dnd_tenebres.domain.effect.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "type")
public class ActiveEffect {

    @Enumerated(EnumType.STRING)
    @Column(name = "effect_type",nullable=false)
    private EffectType type;

    @Column(name = "duration")
    private int duration;

    @Column(name = "power")
    private int power;

    public void decrementDuration() {
        if (this.duration > 0) {
            this.duration--;
        }
    }

    public void reducePower(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Урон по эффекту не может быть отрицательным!");
        }
        this.power = Math.max(0, this.power - amount);
    }
}
