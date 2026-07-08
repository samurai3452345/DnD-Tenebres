package com.java_dragons.dnd_tenebres.domain.location.model;

import lombok.Getter;

@Getter
public enum LocationEffect {
    NONE(false,false), // Без эффекта
    COZY_TAVERN(true,false), // Вешается в некоторых тавернах, дает бонес к макс хп до следующего отдыха
    TOXIC_FUMES(false,true), // Запрет на естественный реген, короткий отдых не востонавливает хп
    DARKNESS(false,true), // Враги всегда ходят аервыми, нельзя найти секреты под этим эфектом(штраф к восприятию)
    ANTI_MAGIC_FIELD(false,true); // Стоимость заклинаний в мане х2

    private final boolean isBuff;
    private final boolean isDebuff;

    LocationEffect(boolean isBuff, boolean isDebuff) {
        this.isBuff = isBuff;
        this.isDebuff = isDebuff;
    }
}
