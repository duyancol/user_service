package com.codeforge.user_service.repository;

import com.codeforge.user_service.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository
        extends JpaRepository<Inventory, Long> {

    List<Inventory> findByUserId(Long userId);

    Optional<Inventory> findByUserIdAndItemId(Long userId, String itemId);
}
