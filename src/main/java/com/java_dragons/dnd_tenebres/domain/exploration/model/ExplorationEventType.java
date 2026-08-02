package com.java_dragons.dnd_tenebres.domain.exploration.model;

public enum ExplorationEventType {
    MOVED,             // Успешно перешел в новую локацию
    COMBAT_STARTED,    // Наткнулся на врага
    FOUND_LOOT,        // Нашел предметы
    NOTHING_FOUND,     // Ничего не произошло
    ERROR              // Ошибка (нет пути, заперто и т.д.)
}