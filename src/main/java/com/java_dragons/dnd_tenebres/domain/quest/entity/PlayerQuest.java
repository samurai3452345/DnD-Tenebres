package com.java_dragons.dnd_tenebres.domain.quest.entity;


import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.quest.model.QuestStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    private PlayerQuest(Player player, QuestTemplate questTemplate) {
        this.questStatus = QuestStatus.ACTIVE;
        this.currentProgress = 0;
        this.questTemplate = questTemplate;
        this.player = player;
    }

    public static PlayerQuest create(Player player, QuestTemplate questTemplate) {

        PlayerQuest playerQuest = new PlayerQuest(player, questTemplate);

        return playerQuest;

    }
    public int getRewardXp() {
        return questTemplate.getRewardXp();
    }

    public int getRewardGold() {
        return questTemplate.getRewardGold();
    }
}
