package com.java_dragons.dnd_tenebres.domain.quest.dto;

import com.java_dragons.dnd_tenebres.domain.player.dto.LevelUpResult;

public record QuestRewardResponse(long gold, LevelUpResult progression,
                                  Long itemTemplateId, int itemAmount, boolean alreadyClaimed) {}
