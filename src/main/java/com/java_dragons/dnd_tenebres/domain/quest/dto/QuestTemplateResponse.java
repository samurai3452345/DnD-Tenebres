package com.java_dragons.dnd_tenebres.domain.quest.dto;

public record QuestTemplateResponse(Long id, String name, String description, String type, String source,
                                    String targetIdentifier, int targetCount, int rewardXp,
                                    int rewardGold, int minLevel, String acceptLocationId,
                                    String turnInLocationId, Long prerequisiteQuestId,
                                    boolean repeatable, Long rewardItemTemplateId, int rewardItemAmount) {}
