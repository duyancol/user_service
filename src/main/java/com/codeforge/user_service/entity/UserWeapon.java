package com.codeforge.user_service.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_weapon")
public class UserWeapon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String weaponId; // 🔥 đổi sang String

    private int enhanceLevel;
    private int level = 1;        // cấp hiện tại
    private int exp = 0;          // exp hiện tại


    private int ascend = 0;       // bậc (0 -> 5 chẳng hạn)
    // getter setter
}
