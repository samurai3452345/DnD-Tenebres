package com.java_dragons.dnd_tenebres.domain.item.entity;

import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.EquipmentSlot;
import com.java_dragons.dnd_tenebres.domain.item.model.MagicWeaponEffect;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "magic_effect", nullable = false)
    @Builder.Default
    private MagicWeaponEffect magicEffect = MagicWeaponEffect.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "magic_effect_element")
    private DamageType magicEffectElement;

    @Column(name = "item_xp", nullable = false)
    @Builder.Default
    private long itemXp = 0;

    @Column(name = "tier", nullable = false)
    @Builder.Default
    private int tier = 1;

    public void addXp(int xp, int newTier) {
        if (xp < 0) throw new IllegalArgumentException("Опыт не может быть отрицательным");
        this.itemXp += xp;

        if (newTier > this.tier) {
            this.tier = newTier;
        }
    }

    public int getTotalDiceCount() {
        if (this.template.getDiceCount() == 0) return 0;
        return this.template.getDiceCount() + (this.tier - 1);
    }
}