package com.codeforge.user_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EquipmentResponse {

    private Long id;
    private String itemId;
    private String quality;

    private String mainStat;
    private int mainValue;

    private String subStatsJson;

    private boolean equipped;
}
