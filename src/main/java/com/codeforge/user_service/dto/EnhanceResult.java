package com.codeforge.user_service.dto;

public class EnhanceResult {
    public boolean success;
    public int newLevel;
    public int rate;
    public int roll;
    public int remainStone;
    public EnhanceResult(boolean success, int newLevel, int rate, int roll) {
        this.success = success;
        this.newLevel = newLevel;
        this.rate = rate;
        this.roll = roll;
    }
    public EnhanceResult(boolean success, int newLevel, int rate, int roll, int remainStone) {
        this.success = success;
        this.newLevel = newLevel;
        this.rate = rate;
        this.roll = roll;
        this.remainStone = remainStone;
    }
}
