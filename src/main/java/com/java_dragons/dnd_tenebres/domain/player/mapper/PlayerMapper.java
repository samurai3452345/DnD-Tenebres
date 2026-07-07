package com.java_dragons.dnd_tenebres.domain.player.mapper;

import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerResponse;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlayerMapper {

    @Mapping(source = "id", target = "playerId")
    @Mapping(source = "name", target = "playerName")
    PlayerResponse toResponse(Player player);

}