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
    @Autowired
    private WeaponConfigService weaponConfigService;
    private Random random = new Random();
    @Autowired
    private CurrencyService currencyService;
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
        weapon.setLevel(1);
        return repo.save(weapon);
    }

    public UserWeapon levelUp(Long userId, String weaponId, int expStoneUse) {

        UserWeapon weapon = getWeapon(userId, weaponId);

        if (weapon.getLevel() >= 100) {
            throw new RuntimeException("Đã max level");
        }

        // 🔥 check chặn tiến bậc
        int requiredAscend = weapon.getLevel() / 20;
        if (weapon.getAscend() < requiredAscend) {
            throw new RuntimeException("Cần tiến bậc để tăng cấp tiếp");
        }

        // 👉 lấy đá exp
        var expItemOpt = inventoryRepository.findByUserIdAndItemId(userId, "item_exp");

        if (expItemOpt.isEmpty()) {
            throw new RuntimeException("Không có đá exp");
        }

        var expItem = expItemOpt.get();

        if (expItem.getAmount() < expStoneUse) {
            throw new RuntimeException("Không đủ đá exp");
        }

        // 🔥 mỗi đá = 100 exp (tuỳ game m chỉnh)
        int gainedExp = expStoneUse * 10;

        weapon.setExp(weapon.getExp() + gainedExp);

        // 🔥 trừ item
        expItem.setAmount(expItem.getAmount() - expStoneUse);
        inventoryRepository.save(expItem);

        // 🔥 level up loop
        while (weapon.getExp() >= getRequiredExp(weapon.getLevel())
                && weapon.getLevel() < 100) {

            weapon.setExp(weapon.getExp() - getRequiredExp(weapon.getLevel()));
            weapon.setLevel(weapon.getLevel() + 1);

            // ❗ chặn tại mốc 20/40/60/80
            if (weapon.getLevel() % 20 == 0) {
                break;
            }
        }

        return repo.save(weapon);
    }

    public UserWeapon ascend(Long userId, String weaponId) {

        UserWeapon weapon = getWeapon(userId, weaponId);

        int level = weapon.getLevel();
        int currentAscend = weapon.getAscend();

        // 🔥 check level
        if (level < (currentAscend + 1) * 20) {
            throw new RuntimeException("Chưa đủ cấp để tiến bậc");
        }

        // 🔥 lấy config
        WeaponUpgradeConfig cfg = weaponConfigService.get(weaponId);
        AscendCost cost = cfg.getAscendCosts().get(currentAscend);

        if (cost == null) {
            throw new RuntimeException("Không có config tiến bậc");
        }

        // 🔥 1. CHECK GOLD
        var currency = currencyService.getCurrency(userId);

        if (currency.getGold() < cost.getGold()) {
            throw new RuntimeException("Không đủ vàng");
        }

        // 🔥 2. CHECK MATERIALS
        for (AscendMaterial mat : cost.getMaterials()) {

            var itemOpt = inventoryRepository
                    .findByUserIdAndItemId(userId, mat.getItemId());

            if (itemOpt.isEmpty() || itemOpt.get().getAmount() < mat.getAmount()) {
                throw new RuntimeException("Thiếu nguyên liệu: " + mat.getItemId());
            }
        }

        // 🔥 3. TRỪ GOLD
        currencyService.subtractGold(userId, cost.getGold());
        // 🔥 4. TRỪ MATERIAL
        for (AscendMaterial mat : cost.getMaterials()) {

            var item = inventoryRepository
                    .findByUserIdAndItemId(userId, mat.getItemId())
                    .get();

            item.setAmount(item.getAmount() - mat.getAmount());

            if (item.getAmount() <= 0) {
                inventoryRepository.delete(item);
            } else {
                inventoryRepository.save(item);
            }
        }

        // 🔥 5. tăng bậc
        weapon.setAscend(currentAscend + 1);
        weapon.setLevel(weapon.getLevel() + 1);
        weapon.setExp(0);
        return repo.save(weapon);
    }

    private int getRequiredExp(int level) {
        return 100 + level * 20; // scale nhẹ
    }

    public AscendCost getAscendCost(Long userId, String weaponId) {

        UserWeapon weapon = getWeapon(userId, weaponId);

        int currentAscend = weapon.getAscend();

        WeaponUpgradeConfig cfg = weaponConfigService.get(weaponId);

        if (cfg.getAscendCosts() == null) {
            throw new RuntimeException("AscendCosts bị null");
        }

        AscendCost cost = cfg.getAscendCosts().get(currentAscend);

        if (cost == null) {
            throw new RuntimeException("Không có config cho ascend level: " + currentAscend);
        }

        return cost;
    }
}