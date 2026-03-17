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

    // getter setter
}
