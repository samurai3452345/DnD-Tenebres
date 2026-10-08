package com.java_dragons.dnd_tenebres.domain.quest.entity;


import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.quest.model.QuestStatus;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "player_quests", uniqueConstraints = @UniqueConstraint(
        name = "uq_active_quest_key", columnNames = {"player_id", "quest_template_id", "active_quest_key"}))
public class PlayerQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    @JsonIgnore
    private Player player;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_template_id", nullable = false)
    private QuestTemplate questTemplate;

    @Column(name = "current_progress", nullable = false)
    private int currentProgress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QuestStatus questStatus;

    @Column(name = "active_quest_key")
    private Boolean activeQuestKey;

    public void incrementProgress(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше нуля");
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
            throw new IllegalStateException("Для получения награды квест должен быть завершён");
        }
        this.questStatus = QuestStatus.REWARDED;
        this.activeQuestKey = null;
    }

    private PlayerQuest(Player player, QuestTemplate questTemplate) {
        this.questStatus = QuestStatus.ACTIVE;
        this.activeQuestKey = true;
        this.currentProgress = 0;
        this.questTemplate = questTemplate;
        this.player = player;
    }

    public static PlayerQuest create(Player player, QuestTemplate questTemplate) {

        return new PlayerQuest(player, questTemplate);

    }
    public int getRewardXp() {
        return questTemplate.getRewardXp();
    }

    public int getRewardGold() {
        return questTemplate.getRewardGold();
    }

    @PrePersist
    @PreUpdate
    private void synchronizeActiveKey() {
        activeQuestKey = questStatus == QuestStatus.REWARDED ? null : Boolean.TRUE;
    }
}
