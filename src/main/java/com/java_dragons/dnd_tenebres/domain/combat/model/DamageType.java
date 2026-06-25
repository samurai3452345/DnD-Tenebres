package com.java_dragons.dnd_tenebres.domain.combat.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
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
}
