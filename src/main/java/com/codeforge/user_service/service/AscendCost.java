package com.codeforge.user_service.service;


import lombok.Data;

import java.util.List;
@Data
public class AscendCost {
    private int gold;
    private List<AscendMaterial> materials;

    // getter/setter
}