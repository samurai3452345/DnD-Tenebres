package com.java_dragons.dnd_tenebres.domain.quest.entity;


import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.quest.model.QuestStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "player_quests")
public class PlayerQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_template_id", nullable = false)
    private QuestTemplate questTemplate;

    @Column(name = "current_progress", nullable = false)
    private int currentProgress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QuestStatus questStatus;

    public void incrementProgress(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (this.currentProgress >= questTemplate.getTargetCount()) {
            return;
        }
        this.currentProgress += amount;
        if (this.currentProgress >= questTemplate.getTargetCount()) {
            this.currentProgress = questTemplate.getTargetCount();
            questStatus = QuestStatus.COMPLETED;
        }
    }
    public void markAsRewarded() {
        if (this.questStatus != QuestStatus.COMPLETED) {
            throw new IllegalStateException("Quest status must be COMPLETED");
        }
        this.questStatus = QuestStatus.REWARDED;
    }
}
