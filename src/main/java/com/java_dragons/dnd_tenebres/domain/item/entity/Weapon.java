package com.java_dragons.dnd_tenebres.domain.item.entity;

import com.java_dragons.dnd_tenebres.domain.item.model.DiceType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@DiscriminatorValue("WEAPON")
@Getter
@Setter
public class Weapon extends Item{

    @Column(name = "base_dice_type")
    @Enumerated(EnumType.STRING)
    private DiceType baseDiceType;


    @Override
    public ItemType getItemType() {
        return ItemType.WEAPON;
    }
}
