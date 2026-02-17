package com.codeforge.user_service.entity;



import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class EquipmentInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String itemId;
    private String quality;
    private String mainStat;
    private int mainValue;

    @Column(columnDefinition = "TEXT")
    private String subStatsJson;

    private boolean equipped = false;
}
