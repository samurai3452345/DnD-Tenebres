package com.java_dragons.dnd_tenebres.domain.item.dto;

import java.util.List;

public record ItemUpgradeRequest(
        Long targetItemId,
        List<Long> foodItemIds
) {}