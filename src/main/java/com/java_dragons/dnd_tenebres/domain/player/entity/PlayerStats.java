package com.java_dragons.dnd_tenebres.domain.player.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Embeddable
public class PlayerStats {

    public static final int MAX_BASE_STAT = 30;

    @Column(name = "stat_str", nullable = false)
    private int strength;       //Сила (STR)

    @Column(name = "stat_dex", nullable = false)
    private int dexterity;      //Ловкость (DEX)

    @Column(name = "stat_con", nullable = false)
    private int constitution;   //Телосложение (CON)

    @Column(name = "stat_int", nullable = false)
    private int intelligence;   //Интеллект (INT)

    @Column(name = "stat_wis", nullable = false)
    private int wisdom;         //Мудрость (WIS)

    @Column(name = "stat_cha", nullable = false)
    private int charisma;       //Харизма (CHA)

    public void addStats(int addStr, int addDex, int addCon, int addInt, int addWis, int addCha) {
        validateIncrease("силы", this.strength, addStr);
        validateIncrease("ловкости", this.dexterity, addDex);
        validateIncrease("телосложения", this.constitution, addCon);
        validateIncrease("интеллекта", this.intelligence, addInt);
        validateIncrease("мудрости", this.wisdom, addWis);
        validateIncrease("харизмы", this.charisma, addCha);

        this.strength = Math.addExact(this.strength, addStr);
        this.dexterity = Math.addExact(this.dexterity, addDex);
        this.constitution = Math.addExact(this.constitution, addCon);
        this.intelligence = Math.addExact(this.intelligence, addInt);
        this.wisdom = Math.addExact(this.wisdom, addWis);
        this.charisma = Math.addExact(this.charisma, addCha);
    }

    private void validateIncrease(String statName, int currentValue, int increase) {
        if (increase < 0) {
            throw new IllegalArgumentException("Добавка к характеристике не может быть отрицательной");
        }
        if ((long) currentValue + increase > MAX_BASE_STAT) {
            throw new IllegalStateException(
                    "Базовое значение " + statName + " не может превышать " + MAX_BASE_STAT
            );
        }
    }

}
