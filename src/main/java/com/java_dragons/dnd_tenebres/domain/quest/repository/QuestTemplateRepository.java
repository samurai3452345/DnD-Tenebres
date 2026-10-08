package com.java_dragons.dnd_tenebres.domain.quest.repository;

import com.java_dragons.dnd_tenebres.domain.quest.entity.QuestTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestTemplateRepository extends JpaRepository<QuestTemplate, Long> {
    List<QuestTemplate> findAllByOrderByIdAsc();
}
