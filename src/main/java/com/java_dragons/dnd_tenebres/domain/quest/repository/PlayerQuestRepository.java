package com.java_dragons.dnd_tenebres.domain.quest.repository;

import com.java_dragons.dnd_tenebres.domain.quest.entity.PlayerQuest;
import com.java_dragons.dnd_tenebres.domain.quest.model.QuestStatus;
import com.java_dragons.dnd_tenebres.domain.quest.model.QuestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;


public interface PlayerQuestRepository extends JpaRepository<PlayerQuest, Long> {

   boolean existsByPlayerIdAndQuestTemplateId(Long playerId, Long questTemplateId);

   @EntityGraph(attributePaths = "questTemplate")
   List<PlayerQuest> findByPlayerIdAndQuestStatusIn(Long playerId, List<QuestStatus> questStatus);

   List<PlayerQuest> findByPlayerIdAndQuestStatusAndQuestTemplateQuestTypeAndQuestTemplateTargetIdentifier(Long playerId, QuestStatus questStatus, QuestType questType, String targetIdentifier);

   Optional<PlayerQuest> findByPlayerIdAndId(Long playerId, Long playerQuestId);
   boolean existsByPlayerIdAndQuestTemplateIdAndQuestStatus(Long playerId, Long questTemplateId, QuestStatus status);
   long countByPlayerIdAndQuestStatusIn(Long playerId, List<QuestStatus> statuses);
   boolean existsByPlayerIdAndQuestTemplateIdAndQuestStatusIn(Long playerId, Long questTemplateId, List<QuestStatus> statuses);
}
