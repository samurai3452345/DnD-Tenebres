package com.java_dragons.dnd_tenebres.domain.item.entity;

import com.java_dragons.dnd_tenebres.domain.item.model.EquipmentSlot;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "player_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ItemTemplate template;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "is_equipped", nullable = false)
    private boolean isEquipped;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipped_slot", nullable = false)
    @Builder.Default
    private EquipmentSlot equippedSlot = EquipmentSlot.NONE;

    @Column(name = "bonus_strength", nullable = false)
    private int bonusStrength;

    @Column(name = "bonus_dexterity", nullable = false)
    private int bonusDexterity;

    @Column(name = "bonus_constitution", nullable = false)
    private int bonusConstitution;

    @Column(name = "bonus_intelligence", nullable = false)
    private int bonusIntelligence;

    @Column(name = "bonus_wisdom", nullable = false)
    private int bonusWisdom;

    @Column(name = "bonus_charisma", nullable = false)
    private int bonusCharisma;
}