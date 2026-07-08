package com.java_dragons.dnd_tenebres.domain.location.model;

import lombok.Getter;

@Getter
public enum LocationType {
    DANGEROUS(true,false),
    NEUTRAL(true,false),
    SAFE_ZONE(true,true);

    private final boolean shortRest;
    private final boolean longRest;

    LocationType(boolean shortRest, boolean longRest) {
        this.shortRest = shortRest;
        this.longRest = longRest;
    }
}
