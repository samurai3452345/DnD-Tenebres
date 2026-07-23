package com.java_dragons.dnd_tenebres.domain.player.entity;

import com.java_dragons.dnd_tenebres.core.math.StatMathUtils;
import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.effect.model.ActiveEffect;
import com.java_dragons.dnd_tenebres.domain.effect.model.EffectType;
import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.EquipmentSlot;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@Entity
@Table(name = "players")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "name", nullable = false)
    private String name;

    @Builder.Default
    @Column(name = "level")
    private int level = 1;

    @Builder.Default
    @Column(name = "experience")
    private long experience = 0;

    @Builder.Default
    @Column(name = "gold")
    private long gold = 0;

    @Column(name = "current_hp")
    private int currentHp;

    @Column(name = "max_hp", nullable = false)
    private int maxHp;

    @Column(name = "current_mp")
    private int currentMp;

    @Column(name = "max_mp", nullable = false)
    private int maxMp;

    @Embedded
    private PlayerStats stats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_location_id")
    private Location currentLocation;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "player_effects", joinColumns = @JoinColumn(name = "player_id"))
    @Builder.Default
    private Set<ActiveEffect> activeEffects = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerItem> inventory = new ArrayList<>();

    public void addExperience(long xp) {
        if (xp < 0) throw new IllegalArgumentException();
        this.experience += xp;
    }

    public void addGold(long amount) {
        if (amount < 0) throw new IllegalArgumentException();
        this.gold += amount;
    }

    public boolean spendGold(long amount) {
        if (amount < 0) throw new IllegalArgumentException();
        if (this.gold < amount) return false;
        this.gold -= amount;
        return true;
    }

    public void healToFull() {
        this.currentHp = this.maxHp;
    }

    public void buffMaxHp(int percent) {
        int bonus = (this.maxHp * percent) / 100;
        this.maxHp += bonus;
        this.currentHp += bonus;
    }

    public void takeDamage(int damage) {
        this.currentHp = Math.max(0, this.currentHp - damage);
    }

    public void removeEffect(EffectType type) {
        this.activeEffects.removeIf(e -> e.getType() == type);
    }

    public void clearEffects() {
        this.activeEffects.clear();
    }

    public boolean hasEffect(EffectType type) {
        return this.activeEffects.stream().anyMatch(e -> e.getType() == type);
    }

    public void moveTo(Location newLocation) {
        if (newLocation == null) throw new IllegalArgumentException();
        this.currentLocation = newLocation;
    }

    public void equipItem(Long playerItemId, EquipmentSlot targetSlot) {
        PlayerItem itemToEquip = this.inventory.stream()
                .filter(item -> Objects.equals(item.getId(), playerItemId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

        ItemType type = itemToEquip.getTemplate().getType();
        if (type == ItemType.RESOURCE || type == ItemType.CONSUMABLE) {
            throw new IllegalStateException();
        }

        if (this.stats.getStrength() < itemToEquip.getTemplate().getRequiredStrength()) {
            throw new IllegalStateException();
        }

        if (!isSlotCompatible(itemToEquip.getTemplate().getSlot(), targetSlot)) {
            throw new IllegalArgumentException();
        }

        this.inventory.stream()
                .filter(PlayerItem::isEquipped)
                .filter(item -> item.getEquippedSlot() == targetSlot)
                .findFirst()
                .ifPresent(oldItem -> {
                    oldItem.setEquipped(false);
                    oldItem.setEquippedSlot(EquipmentSlot.NONE);
                });

        itemToEquip.setEquipped(true);
        itemToEquip.setEquippedSlot(targetSlot);
    }

    private boolean isSlotCompatible(EquipmentSlot templateSlot, EquipmentSlot targetSlot) {
        if (templateSlot == targetSlot) return true;
        return templateSlot == EquipmentSlot.RING &&
                (targetSlot == EquipmentSlot.RING_1 || targetSlot == EquipmentSlot.RING_2);
    }

    public Optional<PlayerItem> getMainHandWeapon() {
        return this.inventory.stream()
                .filter(PlayerItem::isEquipped)
                .filter(item -> item.getEquippedSlot() == EquipmentSlot.MAIN_HAND)
                .findFirst();
    }

    private int getBonusFromEquipment(ToIntFunction<PlayerItem> statExtractor) {
        return this.inventory.stream()
                .filter(PlayerItem::isEquipped)
                .mapToInt(statExtractor)
                .sum();
    }

    public int getTotalStrength() {
        return this.stats.getStrength() + getBonusFromEquipment(PlayerItem::getBonusStrength);
    }

    public int getTotalDexterity() {
        return this.stats.getDexterity() + getBonusFromEquipment(PlayerItem::getBonusDexterity);
    }

    public int getTotalIntelligence() { return this.stats.getIntelligence() + getBonusFromEquipment(PlayerItem::getBonusIntelligence); }
    public int getTotalWisdom() { return this.stats.getWisdom() + getBonusFromEquipment(PlayerItem::getBonusWisdom); }
    public int getTotalCharisma() { return this.stats.getCharisma() + getBonusFromEquipment(PlayerItem::getBonusCharisma); }
    public int getTotalConstitution() { return this.stats.getConstitution() + getBonusFromEquipment(PlayerItem::getBonusConstitution); }

    public int getArmorClass() {
        int totalDex = getTotalDexterity();
        int dexMod = StatMathUtils.calculateModifier(totalDex);

        int offHandAc = this.inventory.stream()
                .filter(PlayerItem::isEquipped)
                .filter(item -> item.getEquippedSlot() == EquipmentSlot.OFF_HAND)
                .mapToInt(item -> item.getTemplate().getArmorClass())
                .sum();

        Optional<PlayerItem> chestArmor = this.inventory.stream()
                .filter(PlayerItem::isEquipped)
                .filter(item -> item.getEquippedSlot() == EquipmentSlot.CHEST)
                .findFirst();

        if (chestArmor.isEmpty()) {
            return 7 + dexMod + offHandAc;
        }

        ItemTemplate armorTemplate = chestArmor.get().getTemplate();
        int armorBaseAc = armorTemplate.getArmorClass();

        int coreAc = switch (armorTemplate.getArmorType()) {
            case LIGHT -> armorBaseAc + dexMod;
            case MEDIUM -> armorBaseAc + Math.min(dexMod, 2);
            case HEAVY -> armorBaseAc;
            case NONE -> 7 + dexMod;
        };

        return coreAc + offHandAc;
    }

    public int getMaxHp() {
        int calculatedMaxHp = this.maxHp;
        Set<ItemPassive> passives = getActivePassives();

        if (passives.contains(ItemPassive.DARK_PACT)) {
            calculatedMaxHp = (int) (calculatedMaxHp * 0.70);
        }

        return calculatedMaxHp;
    }

    public Set<ItemPassive> getActivePassives() {
        Set<ItemPassive> activePassives = new HashSet<>();

        Map<ItemPassive, List<PlayerItem>> equippedByPassive = this.inventory.stream()
                .filter(PlayerItem::isEquipped)
                .filter(item -> item.getTemplate().getPassiveEffect() != ItemPassive.NONE)
                .collect(Collectors.groupingBy(item -> item.getTemplate().getPassiveEffect()));

        for (Map.Entry<ItemPassive, List<PlayerItem>> entry : equippedByPassive.entrySet()) {
            ItemPassive passive = entry.getKey();
            List<PlayerItem> itemsWithPassive = entry.getValue();

            long armorCount = itemsWithPassive.stream()
                    .filter(i -> i.getTemplate().getType() == ItemType.ARMOR)
                    .count();

            if (itemsWithPassive.size() > armorCount || armorCount >= 3) {
                activePassives.add(passive);
            }
        }
        return activePassives;
    }

    public void heal(int amount) {
        if (amount < 0) throw new IllegalArgumentException();
        this.currentHp = Math.min(this.getMaxHp(), this.currentHp + amount);
    }

    public void restoreMp(int amount) {
        if (amount < 0) throw new IllegalArgumentException();
        this.currentMp = Math.min(this.maxMp, this.currentMp + amount);
    }

    public boolean spendMp(int amount) {
        if (amount < 0) throw new IllegalArgumentException();
        if (this.currentMp >= amount) {
            this.currentMp -= amount;
            return true;
        }
        return false;
    }

    public void addEffect(ActiveEffect effect) {
        if (effect == null) throw new IllegalArgumentException();
        this.activeEffects.add(effect);
    }

    public void processTurnEffects(List<CombatEvent> events) {
        Iterator<ActiveEffect> iterator = this.activeEffects.iterator();
        while (iterator.hasNext()) {
            ActiveEffect effect = iterator.next();

            if (effect.getType() == EffectType.REGENERATION) {
                int oldHp = this.currentHp;
                this.heal(effect.getPower());
                int healed = this.currentHp - oldHp;
                effect.decrementDuration();

                events.add(new CombatEvent(this.name, "EFFECT_HEAL", this.name, healed, "Регенерация"));

                if (effect.getDuration() <= 0) {
                    iterator.remove();
                }
            }
        }
    }

    public void consumeItem(PlayerItem item) {
        if (item.getAmount() > 0) {
            item.setAmount(item.getAmount() - 1);
        }
        if (item.getAmount() <= 0) {
            this.inventory.remove(item);
        }
    }
}