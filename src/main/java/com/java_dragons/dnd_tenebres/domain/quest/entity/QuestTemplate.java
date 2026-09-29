package com.java_dragons.dnd_tenebres.domain.quest.entity;

import com.java_dragons.dnd_tenebres.domain.quest.model.QuestType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "quest_templates")
public class QuestTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "quest_type", nullable = false)
    private QuestType questType;

    @Column(name = "target_identifier", nullable = false)
    private String targetIdentifier;

    @Column(name = "target_count", nullable = false)
    private int targetCount;

    @Column(name = "reward_xp", nullable = false)
    private int rewardXp;

    @Column(name = "reward_gold", nullable = false)
    private int rewardGold;

    @Column(name = "min_level", nullable = false)
    private int minLevel;

    @Column(name = "accept_location_id")
    private String acceptLocationId;

    @Column(name = "turn_in_location_id")
    private String turnInLocationId;

    @Column(name = "prerequisite_quest_id")
    private Long prerequisiteQuestId;

    @Column(name = "repeatable", nullable = false)
    private boolean repeatable;

    @Column(name = "remote_turn_in", nullable = false)
    private boolean remoteTurnIn;

    @Column(name = "reward_item_template_id")
    private Long rewardItemTemplateId;

    @Column(name = "reward_item_amount", nullable = false)
    private int rewardItemAmount;

}
