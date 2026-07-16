package com.java_dragons.dnd_tenebres.domain.item.service;

import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.EquipmentSlot;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    @Transactional
    public void equipItem(Player player, Long playerItemId, EquipmentSlot targetSlot) {

        PlayerItem itemToEquip = player.getInventory().stream()
                .filter(item -> java.util.Objects.equals(item.getId(), playerItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Предмет не найден в инвентаре"));

        ItemType type = itemToEquip.getTemplate().getType();
        if (type == ItemType.RESOURCE || type == ItemType.CONSUMABLE) {
            throw new IllegalStateException("Этот предмет нельзя экипировать!");
        }

        if (player.getStats().getStrength() < itemToEquip.getTemplate().getRequiredStrength()) {
            throw new IllegalStateException("Не хватает силы для экипировки: требуется " +
                    itemToEquip.getTemplate().getRequiredStrength() + " Силы.");
        }

        if (!isSlotCompatible(itemToEquip.getTemplate().getSlot(), targetSlot)) {
            throw new IllegalArgumentException("Этот предмет нельзя надеть в слот: " + targetSlot);
        }

        player.getInventory().stream()
                .filter(PlayerItem::isEquipped)
                .filter(item -> item.getEquippedSlot() == targetSlot)
                .findFirst()
                .ifPresent(oldItem -> {
                    oldItem.setEquipped(false);
                    oldItem.setEquippedSlot(EquipmentSlot.NONE);
                });

        itemToEquip.setEquipped(true);
        itemToEquip.setEquippedSlot(targetSlot);

        System.out.println("🗡️ " + player.getName() + " экипирует: " + itemToEquip.getTemplate().getName() + " в слот " + targetSlot);
    }


    private boolean isSlotCompatible(EquipmentSlot templateSlot, EquipmentSlot targetSlot) {
        if (templateSlot == targetSlot) {
            return true;
        }

        if (templateSlot == EquipmentSlot.RING &&
                (targetSlot == EquipmentSlot.RING_1 || targetSlot == EquipmentSlot.RING_2)) {
            return true;
        }

        return false;
    }
}