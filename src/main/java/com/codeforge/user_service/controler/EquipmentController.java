package com.codeforge.user_service.controler;

import com.codeforge.user_service.dto.EquipmentResponse;
import com.codeforge.user_service.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    // ================= CREATE =================
    @PostMapping("/create")
    public void createEquipment(
            @RequestParam Long userId,
            @RequestParam String itemId) {

        equipmentService.createEquipment(userId, itemId);
    }

    // ================= GET ALL BY USER =================
    @GetMapping
    public List<EquipmentResponse> getByUser(
            @RequestParam Long userId) {

        return equipmentService.getEquipmentsByUser(userId);
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    public EquipmentResponse getById(
            @PathVariable Long id) {

        return equipmentService.getEquipmentById(id);
    }
}
