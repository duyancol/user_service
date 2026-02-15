package com.codeforge.user_service.dto;

import lombok.Data;

@Data
public class AddItemRequest {
    private Long userId;
    private String itemId;
    private int amount;
}
