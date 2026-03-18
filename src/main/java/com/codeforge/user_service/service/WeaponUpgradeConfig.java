package com.codeforge.user_service.service;

import lombok.Data;

import java.util.Map;
@Data
public class WeaponUpgradeConfig {
    private String weaponId;

    // ascendLevel -> cost
    private Map<Integer, AscendCost> ascendCosts;

    // getter/setter
}