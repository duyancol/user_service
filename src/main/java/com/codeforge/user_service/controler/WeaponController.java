package com.codeforge.user_service.controler;

import com.codeforge.user_service.dto.AscendCostResponse;
import com.codeforge.user_service.dto.EnhanceResult;
import com.codeforge.user_service.entity.UserWeapon;
import com.codeforge.user_service.service.WeaponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weapons")
public class WeaponController {

    @Autowired
    private WeaponService service;

    // 👉 GET LV VŨ KHÍ
    @GetMapping("/{weaponId}")
    public UserWeapon getWeapon(
            @RequestParam Long userId,
            @PathVariable String weaponId
    ) {
        return service.getWeapon(userId, weaponId);
    }
    @PostMapping("/give")
    public UserWeapon giveWeapon(
            @RequestParam Long userId,
            @RequestParam String weaponId
    ) {
        return service.giveWeapon(userId, weaponId);
    }
    // 👉 ĐẬP ĐỒ
    @PostMapping("/{weaponId}/enhance")
    public EnhanceResult enhance(
            @RequestParam Long userId,
            @PathVariable String weaponId
    ) {
        return service.enhance(userId, weaponId);
    }
    @PostMapping("/equip")
    public UserWeapon equip(
            @RequestParam Long userId,
            @RequestParam String itemId
    ) {
        return service.equipWeapon(userId, itemId);
    }
    @PostMapping("/{weaponId}/ascend")
    public UserWeapon ascend(
            @RequestParam Long userId,
            @PathVariable String weaponId
    ) {
        return service.ascend(userId, weaponId);
    }
    @GetMapping("/{weaponId}/ascend-cost")
    public AscendCostResponse getAscendCost(
            @RequestParam Long userId,
            @PathVariable String weaponId
    ) {
        var cost = service.getAscendCost(userId, weaponId);

        return new AscendCostResponse(cost.getGold(), cost.getMaterials());
    }
    @PostMapping("/{weaponId}/level-up")
    public UserWeapon levelUp(
            @RequestParam Long userId,
            @PathVariable String weaponId,
            @RequestParam int expStoneUse
    ) {
        return service.levelUp(userId, weaponId, expStoneUse);
    }
}
