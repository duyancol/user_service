package com.codeforge.user_service.config;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EnhanceConfig {

    private static final Map<Integer, Integer> SUCCESS_RATE = new HashMap<>();

    static {
        SUCCESS_RATE.put(0, 100);
        SUCCESS_RATE.put(1, 90);
        SUCCESS_RATE.put(2, 75);
        SUCCESS_RATE.put(3, 60);
        SUCCESS_RATE.put(4, 45);
        SUCCESS_RATE.put(5, 30);
        SUCCESS_RATE.put(6, 20);
        SUCCESS_RATE.put(7, 12);
        SUCCESS_RATE.put(8, 8);
        SUCCESS_RATE.put(9, 5);
        SUCCESS_RATE.put(10, 3);
        SUCCESS_RATE.put(11, 2);
    }

    public int getRate(int level) {
        return SUCCESS_RATE.getOrDefault(level, 1);
    }
}
