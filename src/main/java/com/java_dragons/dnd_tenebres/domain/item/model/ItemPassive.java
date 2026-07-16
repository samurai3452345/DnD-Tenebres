package com.java_dragons.dnd_tenebres.domain.item.model;

public enum ItemPassive {
    NONE,               // Нет эффекта
    BERSERKER_RAGE,     // Увеличивает урон/защиту при низком ХП
    DARK_PACT,          // Жертвует ХП ради огромного урона
    LIGHTBRINGER,       // Доп. урон по монстрам тьмы
    EXECUTIONER,         // Добивает монстров с низким ХП
    SWARM_BREAKER
}