package com.codeforge.user_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeaponConfigService {

    private Map<String, WeaponUpgradeConfig> configs = new HashMap<>();

    @PostConstruct
    public void load() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("weapon_config.json");

            List<WeaponUpgradeConfig> list =
                    mapper.readValue(is, new TypeReference<List<WeaponUpgradeConfig>>() {
                    });

            for (WeaponUpgradeConfig cfg : list) {
                configs.put(cfg.getWeaponId(), cfg);
            }

            System.out.println("✅ Loaded weapon config: " + configs.keySet());

        } catch (Exception e) {
            throw new RuntimeException("❌ Load config fail", e);
        }
    }

    public WeaponUpgradeConfig get(String weaponId) {

        WeaponUpgradeConfig cfg = configs.get(weaponId);

        if (cfg == null) {
            throw new RuntimeException(
                    "❌ Không có config cho weaponId: " + weaponId +
                            " | Available: " + configs.keySet()
            );
        }

        return cfg;
    }
}