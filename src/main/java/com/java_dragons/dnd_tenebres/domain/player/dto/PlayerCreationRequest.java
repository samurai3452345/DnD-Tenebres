package com.java_dragons.dnd_tenebres.domain.player.dto;


public record PlayerCreationRequest(String name , int strength, int dexterity, int constitution, int intelligence, int wisdom, int charisma) {

}
