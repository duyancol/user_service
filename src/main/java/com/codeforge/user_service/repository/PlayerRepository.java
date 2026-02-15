package com.codeforge.user_service.repository;



import com.codeforge.user_service.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Integer> {

    Optional<Player> findByUserId(Integer userId);

}

