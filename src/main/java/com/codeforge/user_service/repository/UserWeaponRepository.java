package com.codeforge.user_service.repository;

import com.codeforge.user_service.entity.UserWeapon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWeaponRepository extends JpaRepository<UserWeapon, Long> {

    Optional<UserWeapon> findByUserIdAndWeaponId(Long userId, String weaponId);
}