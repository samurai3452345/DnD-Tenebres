package com.java_dragons.dnd_tenebres.domain.player.entity;


import com.java_dragons.dnd_tenebres.core.math.StatMathUtils;
import com.java_dragons.dnd_tenebres.domain.effect.model.ActiveEffect;
import com.java_dragons.dnd_tenebres.domain.effect.model.EffectType;
import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.item.model.EquipmentSlot;
import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import jakarta.persistence.*;

import java.util.*;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Embedded
    private PlayerStats stats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_location_id")
    private Location currentLocation;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_effects", joinColumns = @JoinColumn(name = "player_id"))
    private Set<ActiveEffect> activeEffects = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerItem> inventory = new ArrayList<>();

    public void addExperience(long xp) {
        if (xp < 0) {
            throw new IllegalArgumentException("Опыт не может быть отрицательным!");
        }
        this.experience += xp;
    }

    public void addGold(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Нельзя добавить отрицательное золото!");
        }

        this.gold += amount;
    }

    public boolean spendGold(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Сумма не может быть отрицательной!");
        }
        if (this.gold < amount) {
            return false;
        }
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

    public void addEffect(ActiveEffect effect) {
        this.activeEffects.add(effect);
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
        if (newLocation == null) {
            throw new IllegalArgumentException("Локация не может быть пустой!");
        }
        this.currentLocation = newLocation;
    }

    public Optional<PlayerItem> getMainHandWeapon() {
        return this.inventory.stream()
                .filter(PlayerItem::isEquipped) // Только надетые вещи
                .filter(item -> item.getEquippedSlot() == EquipmentSlot.MAIN_HAND)
                .findFirst();
    }

    public int getTotalStrength() {
        int baseStrength = this.stats.getStrength();
        int bonusStrength = this.inventory.stream()
                .filter(PlayerItem::isEquipped) // Берем только то, что надето
                .mapToInt(PlayerItem::getBonusStrength) // Извлекаем бонус силы
                .sum();
        return baseStrength + bonusStrength;
    }

    public int getTotalDexterity() {
        int baseDex = this.stats.getDexterity();
        int bonusDex = this.inventory.stream()
                .filter(PlayerItem::isEquipped)
                .mapToInt(PlayerItem::getBonusDexterity)
                .sum();
        return baseDex + bonusDex;
    }

    //TODO: такие же методы для остальных статов

    public int getArmorClass() {
        int totalDex = getTotalDexterity();
        int dexMod = StatMathUtils.calculateModifier(totalDex);

        java.util.Optional<PlayerItem> chestArmor = this.inventory.stream()
                .filter(PlayerItem::isEquipped)
                .filter(item -> item.getEquippedSlot() == EquipmentSlot.CHEST)
                .findFirst();

        // 3. Если брони нет - базовый AC = 10 + Ловкость
        if (chestArmor.isEmpty()) {
            return 7 + dexMod;
        }

        ItemTemplate armorTemplate = chestArmor.get().getTemplate();
        int armorBaseAc = armorTemplate.getArmorClass();

        return switch (armorTemplate.getArmorType()) {
            case LIGHT -> armorBaseAc + dexMod; // Легкая: полный бонус ловкости
            case MEDIUM -> armorBaseAc + Math.min(dexMod, 2); // Средняя: максимум +2 от ловкости
            case HEAVY -> armorBaseAc; // Тяжелая: ловкость не учитывается вообще
            case NONE -> 7 + dexMod; // Защита от багов, если в грудь надели не-броню
        };
    }

    public int getMaxHp() {
        int calculatedMaxHp = this.maxHp; // Берем базовое значение из БД

        boolean hasDarkPact = this.inventory.stream()
                .filter(com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem::isEquipped)
                .anyMatch(item -> item.getTemplate().getPassiveEffect() == com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive.DARK_PACT);

        if (hasDarkPact) {
            calculatedMaxHp = (int) (calculatedMaxHp * 0.70);
        }
        return calculatedMaxHp;
    }

}
