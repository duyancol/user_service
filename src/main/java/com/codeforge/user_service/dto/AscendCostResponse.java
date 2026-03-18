package com.codeforge.user_service.dto;


import com.codeforge.user_service.service.AscendMaterial;
import lombok.Data;

import java.util.List;
@Data
public class AscendCostResponse {
    private int gold;
    private List<AscendMaterial> materials;

    // constructor
    public AscendCostResponse(int gold, List<AscendMaterial> materials) {
        this.gold = gold;
        this.materials = materials;
    }

    // getter
}