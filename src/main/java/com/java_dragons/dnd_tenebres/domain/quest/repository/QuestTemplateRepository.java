package com.java_dragons.dnd_tenebres.domain.quest.repository;

import com.java_dragons.dnd_tenebres.domain.quest.entity.QuestTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestTemplateRepository extends JpaRepository<QuestTemplate, Long> {
    @Query(value = """
            SELECT *
                FROM quest_templates qt
                    WHERE NOT EXISTS(
                        SELECT 1
                            FROM player_quests pq
                                WHERE pq.quest_template_id = qt.id
                                    AND pq.player_id = :playerId
                        )
            
            """, nativeQuery = true)
    List<QuestTemplate> findAvailableForPlayer(@Param("playerId") Long playerId);
}
