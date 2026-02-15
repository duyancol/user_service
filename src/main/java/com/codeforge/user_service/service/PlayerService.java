package com.codeforge.user_service.service;

import com.codeforge.user_service.entity.Player;
import com.codeforge.user_service.repository.PlayerRepository;
import com.codeforge.user_service.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;





@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    public void createPlayerIfNotExists(User user) {

        Optional<Player> optionalPlayer =
                playerRepository.findByUserId(user.getId());

        if (optionalPlayer.isEmpty()) {

            Player player = Player.builder()
                    .user(user)
                    .name(user.getFirstname())
                    .level(1)
                    .exp(0)
                    .power(10)
                    .build();

            playerRepository.save(player);
        }
    }
    public Player getPlayerByUserId(Integer userId) {
        return playerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
    }

    public Player updatePlayerName(Integer userId, String newName) {
        Player player = getPlayerByUserId(userId);
        player.setName(newName);
        return playerRepository.save(player);
    }

}
