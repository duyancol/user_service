package com.codeforge.user_service.service;

import com.codeforge.user_service.dto.EquipmentResponse;
import com.codeforge.user_service.entity.EquipmentInstance;
import com.codeforge.user_service.entity.Inventory;
import com.codeforge.user_service.repository.EquipmentRepository;
import com.codeforge.user_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final InventoryRepository inventoryRepository;

    private final Random random = new Random();

    // ================= CREATE =================
    public void createEquipment(Long userId, String itemId) {

        EquipmentInstance instance = new EquipmentInstance();
        instance.setUserId(userId);
        instance.setItemId(itemId);

        // ================= RANDOM PHẨM CHẤT =================
        String[] qualities = {"BLUE", "PURPLE", "ORANGE"};
        String quality = qualities[random.nextInt(3)];
        instance.setQuality(quality);

        double multiplier = switch (quality) {
            case "PURPLE" -> 1.5;
            case "ORANGE" -> 2.0;
            default -> 1.0;
        };

        // ================= MAIN STAT THEO LOẠI =================
        String mainStat;
        int baseValue;

        if (itemId.contains("helm")) {
            mainStat = "DEF";
            baseValue = 50;
        } else if (itemId.contains("armor")) {
            mainStat = "HP";
            baseValue = 400;
        } else if (itemId.contains("boots")) {
            mainStat = "CRIT";
            baseValue = 5;
        } else if (itemId.contains("earring")) {
            mainStat = "HP";
            baseValue = 300;
        } else if (itemId.contains("feather")) {
            mainStat = "CRIT_DAMAGE";
            baseValue = 10;
        } else if (itemId.contains("clock")) {
            mainStat = "MDEF";
            baseValue = 40;
        } else {
            mainStat = "HP";
            baseValue = 200;
        }

        int mainValue = (int) (baseValue * multiplier + random.nextInt(20));

        instance.setMainStat(mainStat);
        instance.setMainValue(mainValue);

        // ================= 4 SUB STATS =================
        String[] subTypes = {"HP", "Attack", "DEF", "CRIT", "CRIT_DAMAGE", "MDEF"};

        StringBuilder subStats = new StringBuilder("[");
        for (int i = 0; i < 4; i++) {
            String type = subTypes[random.nextInt(subTypes.length)];
            int value = 5 + random.nextInt(15);

            subStats.append(String.format(
                    "{\"type\":\"%s\",\"value\":%d}",
                    type,
                    value
            ));

            if (i < 3) subStats.append(",");
        }
        subStats.append("]");

        instance.setSubStatsJson(subStats.toString());

        equipmentRepository.save(instance);

        // ================= SAVE INVENTORY =================
        Inventory inventory = new Inventory();
        inventory.setUserId(userId);
        inventory.setItemId(itemId);

        inventory.setEquipmentInstanceId(instance.getId());
        inventory.setAmount(1);

        inventoryRepository.save(inventory);
    }


    // ================= GET ALL BY USER =================
    public List<EquipmentResponse> getEquipmentsByUser(Long userId) {

        return equipmentRepository.findByUserId(userId)
                .stream()
                .map(e -> EquipmentResponse.builder()
                        .id(e.getId())
                        .itemId(e.getItemId())
                        .mainStat(e.getMainStat())
                        .mainValue(e.getMainValue())
                        .subStatsJson(e.getSubStatsJson())
                        .equipped(e.isEquipped())
                        .build())
                .toList();
    }

    // ================= GET BY ID =================
    public EquipmentResponse getEquipmentById(Long id) {

        EquipmentInstance e = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        return EquipmentResponse.builder()
                .id(e.getId())
                .itemId(e.getItemId())
                .quality(e.getQuality())
                .mainStat(e.getMainStat())
                .mainValue(e.getMainValue())
                .subStatsJson(e.getSubStatsJson())
                .equipped(e.isEquipped())
                .build();
    }
}
