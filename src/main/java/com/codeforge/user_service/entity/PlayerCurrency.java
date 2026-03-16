package com.codeforge.user_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "player_currency")
public class PlayerCurrency {

    @Id
    private Long playerId;

    private int gold;
    private int gem;

    private LocalDateTime updatedAt;
}
