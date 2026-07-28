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
    @Mapping(target = "totalStrength", expression = "java(player.getTotalStrength())")
    @Mapping(target = "totalDexterity", expression = "java(player.getTotalDexterity())")
    @Mapping(target = "totalConstitution", expression = "java(player.getTotalConstitution())")
    @Mapping(target = "totalIntelligence", expression = "java(player.getTotalIntelligence())")
    @Mapping(target = "totalWisdom", expression = "java(player.getTotalWisdom())")
    @Mapping(target = "totalCharisma", expression = "java(player.getTotalCharisma())")
    @Mapping(target = "armorClass", expression = "java(player.getArmorClass())")
    PlayerResponse toResponse(Player player);

}