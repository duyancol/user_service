package com.codeforge.user_service.repository;

import com.codeforge.user_service.entity.PlayerCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerCurrencyRepository extends JpaRepository<PlayerCurrency, Long> {
}
