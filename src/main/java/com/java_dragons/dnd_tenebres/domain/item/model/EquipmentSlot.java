package com.java_dragons.dnd_tenebres.domain.item.model;

public enum EquipmentSlot {
    NONE,

    // Броня и оружие
    RIGHT_HAND, // правая рука
    LEFT_HAND,  // левая рука (Оружие)
    HEAD,       // Шлем
    CHEST,      // Нагрудник
    LEGS,       // Поножи

    // Артефакты
    AMULET,     // Амулет
    RING,       // Кольцо (в БД шаблон один, а слотов у игрока потом сделаем два)
    GLOVES,     // Перчатки
    MASK,       // Маска
    SIGIL       // Магическое Клеймо
}