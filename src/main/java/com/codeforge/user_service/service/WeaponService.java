package com.codeforge.user_service.service;

import com.codeforge.user_service.config.EnhanceConfig;
import com.codeforge.user_service.dto.EnhanceResult;
import com.codeforge.user_service.entity.UserWeapon;
import com.codeforge.user_service.repository.InventoryRepository;
import com.codeforge.user_service.repository.UserWeaponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class WeaponService {

    @Autowired
    private UserWeaponRepository repo;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private EnhanceConfig config;

    private Random random = new Random();

    // 👉 LẤY THÔNG TIN
    public UserWeapon getWeapon(Long userId, String weaponId) {
        return repo.findByUserIdAndWeaponId(userId, weaponId)
                .orElseThrow(() -> new RuntimeException("Weapon not found"));
    }

    // 👉 ĐẬP ĐỒ
    public EnhanceResult enhance(Long userId, String weaponId) {

        UserWeapon weapon = getWeapon(userId, weaponId);

        int currentLevel = weapon.getEnhanceLevel();

        // 🔥 1. tính cost
        int needStone = 1 + currentLevel;

        // 🔥 2. lấy đá từ inventory
        var stoneOpt = inventoryRepository
                .findByUserIdAndItemId(userId, "item_dch");

        if (stoneOpt.isEmpty()) {
            throw new RuntimeException("Không có đá cường hóa");
        }

        var stone = stoneOpt.get();

        // ❗ check đủ đá
        if (stone.getAmount() < needStone) {
            throw new RuntimeException("Không đủ đá");
        }

        // 🔥 3. TRỪ ĐÁ
        stone.setAmount(stone.getAmount() - needStone);

        if (stone.getAmount() <= 0) {
            inventoryRepository.delete(stone);
        } else {
            inventoryRepository.save(stone);
        }

        // 🔥 4. roll tỉ lệ
        int rate = config.getRate(currentLevel);
        int roll = random.nextInt(100) + 1;

        boolean success = roll <= rate;

        // 🔥 5. update level
        if (success) {
            weapon.setEnhanceLevel(currentLevel + 1);
        } else {
            // tùy game:
            // weapon.setEnhanceLevel(Math.max(0, currentLevel - 1));
        }

        repo.save(weapon);

        // 🔥 6. return thêm stone còn lại (optional nhưng rất ngon)
        int remainStone = Math.max(0, stone.getAmount());

        return new EnhanceResult(success, weapon.getEnhanceLevel(), rate, roll, remainStone);
    }
    public UserWeapon giveWeapon(Long userId, String weaponId) {

        Optional<UserWeapon> existing = repo.findByUserIdAndWeaponId(userId, weaponId);

        // 👉 nếu đã có thì trả luôn (tránh duplicate)
        if (existing.isPresent()) {
            return existing.get();
        }

        UserWeapon w = new UserWeapon();
        w.setUserId(userId);
        w.setWeaponId(weaponId);
        w.setEnhanceLevel(0);

        return repo.save(w);
    }
    public UserWeapon equipWeapon(Long userId, String itemId) {

        // 👉 check inventory có item không
        var items = inventoryRepository.findByUserIdAndItemId(userId, itemId);

        if (items.isEmpty()) {
            throw new RuntimeException("Không có item để equip");
        }

        // 👉 check đã có weapon chưa
        var weaponOpt = repo.findByUserIdAndWeaponId(userId, itemId);

        if (weaponOpt.isPresent()) {
            return weaponOpt.get();
        }

        // 👉 tạo mới weapon (+0)
        UserWeapon weapon = new UserWeapon();
        weapon.setUserId(userId);
        weapon.setWeaponId(itemId); // 🔥 dùng itemId luôn
        weapon.setEnhanceLevel(0);

        return repo.save(weapon);
    }
}