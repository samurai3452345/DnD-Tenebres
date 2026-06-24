package com.java_dragons.dnd_tenebres.domain.combat.model;

public enum DamageType {
    PHYSICAL("Физический"),
    FIRE("Огонь"),
    WATER("Вода"),
    EARTH("Земля"),
    AIR("Воздух"),
    NATURE("Природа"),
    ICE("Лед"),
    ELECTRICITY("Электричество"),
    LIGHT("Свет"),
    DARK("Тьма");

    private final String displayName;

    DamageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    }
