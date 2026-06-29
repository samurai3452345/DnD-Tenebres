package com.java_dragons.dnd_tenebres.domain.item.entity;


import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import jakarta.persistence.DiscriminatorValue;

@DiscriminatorValue("ARTIFACT")
public class Artifact extends Item{
    @Override
    public ItemType getItemType() {
        return ItemType.ARTIFACT;
    }
}
