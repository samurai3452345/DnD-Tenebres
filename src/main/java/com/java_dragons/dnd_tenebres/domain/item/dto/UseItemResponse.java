package com.java_dragons.dnd_tenebres.domain.item.dto;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import java.util.List;

public record UseItemResponse(Long itemId, boolean consumed, int remainingAmount,
                              int currentHp, int currentMp, List<CombatEvent> events) {}
