package com.java_dragons.dnd_tenebres.domain.item.entity;

import com.java_dragons.dnd_tenebres.domain.item.model.DiceType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@DiscriminatorValue("WEAPON")
public class Weapon extends Item{

    @Column(name = "base_dice_type")
    @Enumerated(EnumType.STRING)
    private DiceType baseDiceType;
}
