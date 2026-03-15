package com.codeforge.user_service.repository;


import com.codeforge.user_service.entity.EquipmentInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<EquipmentInstance, Long> {
    List<EquipmentInstance> findByUserId(Long userId);
    List<EquipmentInstance> findByUserIdAndEquippedTrue(Long userId);
}

