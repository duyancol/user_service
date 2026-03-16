package com.codeforge.user_service.service;

import com.codeforge.user_service.entity.Inventory;
import com.codeforge.user_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public List<Inventory> getInventory(Long userId) {
        return inventoryRepository.findByUserId(userId);
    }

//    public void addItem(Long userId, String itemId, int amount) {
//
//        var slot = inventoryRepository
//                .findByUserIdAndItemId(userId, itemId);
//
//        if (slot.isPresent()) {
//            var inv = slot.get();
//            inv.setAmount(inv.getAmount() + amount);
//            inventoryRepository.save(inv);
//        } else {
//            Inventory inv = new Inventory();
//            inv.setUserId(userId);
//            inv.setItemId(itemId);
//            inv.setAmount(amount);
//            inventoryRepository.save(inv);
//        }
//    }
public void addItem(Long userId, String itemId, int amount) {

    boolean isStackable = itemId.startsWith("item_"); // material
    // equip_ là equipment

    if (isStackable) {

        var slot = inventoryRepository
                .findByUserIdAndItemId(userId, itemId);

        if (slot.isPresent()) {
            var inv = slot.get();
            inv.setAmount(inv.getAmount() + amount);
            inventoryRepository.save(inv);
        } else {
            Inventory inv = new Inventory();
            inv.setUserId(userId);
            inv.setItemId(itemId);
            inv.setAmount(amount);
            inventoryRepository.save(inv);
        }

    } else {

        // equipment → mỗi cái 1 row
        for (int i = 0; i < amount; i++) {

            Inventory inv = new Inventory();
            inv.setUserId(userId);
            inv.setItemId(itemId);
            inv.setAmount(1);

            inventoryRepository.save(inv);
        }
    }
}
    public void removeItem(Long userId, String itemId, int amount) {

        var slot = inventoryRepository
                .findByUserIdAndItemId(userId, itemId)
                .orElseThrow();

        slot.setAmount(slot.getAmount() - amount);

        if (slot.getAmount() <= 0) {
            inventoryRepository.delete(slot);
        } else {
            inventoryRepository.save(slot);
        }
    }
}
