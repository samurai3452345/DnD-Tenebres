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

    @Column(name = "account_id", nullable = false)
    private Long accountId;

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

    @Builder.Default
    @Column(name = "travel_sequence", nullable = false)
    private long travelSequence = 0;

    @Builder.Default
    @Column(name = "last_short_rest_sequence", nullable = false)
    private long lastShortRestSequence = -1;

    @Builder.Default
    @Column(name = "stat_points", nullable = false)
    private int statPoints = 0;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "player_effects", joinColumns = @JoinColumn(name = "player_id"))
    @Builder.Default
    private Set<ActiveEffect> activeEffects = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerItem> inventory = new ArrayList<>();

    public void addExperience(long xp) {
        if (xp < 0) throw new IllegalArgumentException("Опыт не может быть отрицательным");
        this.experience += xp;
    }

    public void addGold(long amount) {
        if (amount < 0) throw new IllegalArgumentException("Количество золота не может быть отрицательным");
        this.gold += amount;
    }

    public long removeGoldPercent(int percent) {
        if (percent < 0 || percent > 100) throw new IllegalArgumentException("Percent must be between 0 and 100");
        long lost = (gold * percent) / 100;
        gold -= lost;
        return lost;
    }

    public void restoreAfterDeath() {
        currentHp = Math.max(1, getMaxHp() / 2);
        currentMp = Math.max(0, getMaxMp() / 2);
        normalizeResources();
    }

    public boolean spendGold(long amount) {
        if (amount < 0) throw new IllegalArgumentException("Количество золота не может быть отрицательным");
        if (this.gold < amount) return false;
        this.gold -= amount;
        return true;
    }

    public void healToFull() {
        this.currentHp = this.getMaxHp();
    }

    public void buffMaxHp(int percent) {
        int bonus = (this.maxHp * percent) / 100;
        this.maxHp += bonus;
        this.currentHp += bonus;
        normalizeResources();
    }

    public void takeDamage(int damage) {
        normalizeResources();
        this.currentHp = Math.max(0, this.currentHp - damage);
    }

    public void removeEffect(EffectType type) {
        this.activeEffects.removeIf(e -> e.getType() == type);
        normalizeResources();
    }

    public void clearEffects() {
        this.activeEffects.clear();
        normalizeResources();
    }

    public boolean hasEffect(EffectType type) {
        return this.activeEffects.stream().anyMatch(e -> e.getType() == type);
    }

    public void moveTo(Location newLocation) {
        if (newLocation == null) throw new IllegalArgumentException("Новая локация обязательна");
        this.currentLocation = newLocation;
        this.travelSequence++;
    }

    public boolean canTakeShortRest() { return lastShortRestSequence != travelSequence; }
    public void markShortRestUsed() { this.lastShortRestSequence = this.travelSequence; }
    public void equipItem(Long playerItemId, EquipmentSlot targetSlot) {
        PlayerItem itemToEquip = this.inventory.stream()
                .filter(item -> Objects.equals(item.getId(), playerItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Предмет не найден в инвентаре игрока"));

        ItemType type = itemToEquip.getTemplate().getType();
        if (type == ItemType.RESOURCE || type == ItemType.CONSUMABLE) {
            throw new IllegalStateException("Расходники и ресурсы нельзя экипировать");
        }

        if (this.stats.getStrength() < itemToEquip.getTemplate().getRequiredStrength()) {
            throw new IllegalStateException("Недостаточно силы для экипировки предмета");
        }

        if (!isSlotCompatible(itemToEquip.getTemplate().getSlot(), targetSlot)) {
            throw new IllegalArgumentException("Предмет нельзя экипировать в выбранный слот");
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
        normalizeResources();
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
        if (amount < 0) throw new IllegalArgumentException("Количество лечения не может быть отрицательным");
        normalizeResources();
        this.currentHp = Math.min(this.getMaxHp(), this.currentHp + amount);
    }

    public void restoreMp(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Количество восстанавливаемой маны не может быть отрицательным");
        normalizeResources();
        this.currentMp = Math.min(this.getMaxMp(), this.getCurrentMp() + amount);
    }

    public void restoreMpToFull() {
        this.currentMp = this.getMaxMp();
    }

    public boolean spendMp(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Расход маны не может быть отрицательным");
        normalizeResources();
        if (this.currentMp >= amount) {
            this.currentMp -= amount;
            return true;
        }
        return false;
    }

    public void addEffect(ActiveEffect effect) {
        if (effect == null) {
            throw new IllegalArgumentException("Эффект обязателен");
        }

        this.activeEffects.stream()
                .filter(existing -> existing.getType() == effect.getType())
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.refresh(
                                effect.getDuration(),
                                effect.getPower()
                        ),
                        () -> this.activeEffects.add(effect)
                );
        normalizeResources();
    }

    public void surviveAtOneHp() { this.currentHp = 1; }

    public void consumeItem(PlayerItem item) {
        if (item.getAmount() > 0) {
            item.setAmount(item.getAmount() - 1);
        }
        if (item.getAmount() <= 0) {
            this.inventory.remove(item);
        }
    }

    public boolean consumeItemByName(String itemName) {
        Optional<PlayerItem> itemOpt = this.inventory.stream()
                .filter(i -> i.getTemplate().getName().equalsIgnoreCase(itemName))
                .filter(i -> i.getAmount() > 0)
                .findFirst();

        if (itemOpt.isPresent()) {
            consumeItem(itemOpt.get());
            return true;
        }
        return false;
    }

    public void unequipItem(EquipmentSlot targetSlot) {
        this.inventory.stream()
                .filter(PlayerItem::isEquipped)
                .filter(item -> item.getEquippedSlot() == targetSlot)
                .findFirst()
                .ifPresent(item -> {
                    item.setEquipped(false);
                    item.setEquippedSlot(EquipmentSlot.NONE);
                });
        normalizeResources();
    }

    public void levelUp(int baseHpForLevel, int baseMpForLevel) {
        this.level++;
        this.statPoints += 1;

        this.maxHp = baseHpForLevel;
        this.maxMp = baseMpForLevel;

        this.currentHp = this.getMaxHp();
        this.currentMp = this.getMaxMp();
    }

    public void allocateStats(int addStr, int addDex, int addCon, int addInt, int addWis, int addCha) {
        if (addStr < 0 || addDex < 0 || addCon < 0 || addInt < 0 || addWis < 0 || addCha < 0) {
            throw new IllegalArgumentException("Добавка к характеристике не может быть отрицательной");
        }
        long totalCost = Math.addExact(
                Math.addExact(Math.addExact((long) addStr, addDex), Math.addExact((long) addCon, addInt)),
                Math.addExact((long) addWis, addCha)
        );

        if (totalCost <= 0) return;
        if (totalCost > this.statPoints) {
            throw new IllegalStateException("Недостаточно поинтов! У вас: " + this.statPoints);
        }

        this.stats.addStats(addStr, addDex, addCon, addInt, addWis, addCha);
        this.statPoints = Math.toIntExact(this.statPoints - totalCost);
        normalizeResources();
    }

    public int getMaxHp() {
        int calculatedHp = this.maxHp;

        int conModifier = StatMathUtils.calculateModifier(this.getTotalConstitution());
        calculatedHp += (conModifier * this.level);

        if (this.hasEffect(EffectType.WELL_RESTED)) {
            calculatedHp += 10;
        }

        Set<ItemPassive> passives = getActivePassives();

        if (passives.contains(ItemPassive.DARK_PACT)) {
            calculatedHp = (int) (calculatedHp * 0.70);
        }

        return Math.max(1, calculatedHp);
    }

    public int getMaxMp() {
        int calculatedMp = this.maxMp;

        int intModifier = StatMathUtils.calculateModifier(this.getTotalIntelligence());
        calculatedMp += (intModifier * 3 * this.level);

        if (this.hasEffect(EffectType.MAGIC_SICKNESS)) {
            calculatedMp = (int) (calculatedMp * 0.50);
        }

        return Math.max(0, calculatedMp);
    }

    public int getCurrentMp() {
        return Math.min(this.currentMp, this.getMaxMp());
    }

    public int getCurrentHp() {
        return Math.min(this.currentHp, this.getMaxHp());
    }

    public void normalizeResources() {
        this.currentHp = Math.max(0, Math.min(this.currentHp, this.getMaxHp()));
        this.currentMp = Math.max(0, Math.min(this.currentMp, this.getMaxMp()));
    }

}
