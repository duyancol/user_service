package com.codeforge.user_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class Item {

    @Id
    private String id; // ví dụ: "potion_hp_small"

    private String name;

    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    private String description;
}

