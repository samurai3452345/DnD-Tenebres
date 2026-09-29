package com.java_dragons.dnd_tenebres.domain.monster.strategy;
public record MonsterDecision(Action action, String ability) {
    public enum Action { BASIC_ATTACK, SPECIAL_ABILITY, DEFEND }
}
