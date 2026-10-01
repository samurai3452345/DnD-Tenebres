package com.java_dragons.dnd_tenebres.domain.quest.dto;

public record PlayerQuestResponse(Long id, QuestTemplateResponse quest, int currentProgress,
                                  int targetCount, String status, boolean canTurnIn) {}
