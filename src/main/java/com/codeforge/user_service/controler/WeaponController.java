package com.codeforge.user_service.controler;

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
}
