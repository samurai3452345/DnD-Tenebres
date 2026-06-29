package com.java_dragons.dnd_tenebres.domain.item.entity;


import com.java_dragons.dnd_tenebres.core.math.ItemProgressionCalculator;
import com.java_dragons.dnd_tenebres.core.math.ItemProgressionCalculatorImpl;
import com.java_dragons.dnd_tenebres.core.math.ProgressionCalculator;
import com.java_dragons.dnd_tenebres.core.math.ProgressionCalculatorImpl;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemRarity;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "items")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_type")
public abstract class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(name = "rarity",  nullable = false)
    private ItemRarity rarity;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "current_xp", nullable = false)
    private long currentXp = 0;

    @Column(name = "is_equipped", nullable = false)
    private boolean isEquipped = false;

    @Column(name = "equip_slot")
    private String equipSlot;

    @Column(name = "parent_item_id")
    private Long parentItemId;

    @Embedded
    private ItemStats stats;

    public void addXp(int xp) {
        this.currentXp += xp;
    }

    @Transient
    public int getCurrentTier(ItemProgressionCalculatorImpl calc){
       return calc.getTierByXp(currentXp);
    }
    public abstract ItemType getItemType();
}
