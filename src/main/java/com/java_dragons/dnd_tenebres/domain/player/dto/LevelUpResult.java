package com.java_dragons.dnd_tenebres.domain.player.dto;

public record LevelUpResult(
        long experienceGranted,
        int oldLevel,
        int newLevel,
        int statPointsGranted) {

    public boolean leveledUp() {
        return newLevel > oldLevel;
    }
}
