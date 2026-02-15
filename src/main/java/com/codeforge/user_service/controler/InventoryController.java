package com.codeforge.user_service.controler;

import com.codeforge.user_service.dto.AddItemRequest;
import com.codeforge.user_service.entity.Inventory;
import com.codeforge.user_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<Inventory> getInventory(
            @RequestParam Long userId) {
        return inventoryService.getInventory(userId);
    }

    @PostMapping("/add")
    public void addItem(@RequestBody AddItemRequest request) {
        inventoryService.addItem(
                request.getUserId(),
                request.getItemId(),
                request.getAmount());
    }

    @PostMapping("/remove")
    public void removeItem(@RequestBody AddItemRequest request) {
        inventoryService.removeItem(
                request.getUserId(),
                request.getItemId(),
                request.getAmount());
    }
}
