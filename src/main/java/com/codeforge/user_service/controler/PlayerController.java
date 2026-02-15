package com.codeforge.user_service.controler;

import com.codeforge.user_service.entity.Player;
import com.codeforge.user_service.service.PlayerService;
import com.codeforge.user_service.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    // ===== GET PLAYER BY USER ID =====
    @GetMapping("/{userId}")
    public ResponseEntity<?> getPlayer(@PathVariable Integer userId) {

        Player player = playerService.getPlayerByUserId(userId);

        return ResponseEntity.ok(Map.of(
                "id", player.getId(),
                "name", player.getName(),
                "level", player.getLevel(),
                "exp", player.getExp(),
                "power", player.getPower()
        ));
    }

    // ===== UPDATE PLAYER NAME =====
    @PutMapping("/{userId}")
    public ResponseEntity<?> updatePlayer(
            @PathVariable Integer userId,
            @RequestBody Map<String, String> body
    ) {

        String newName = body.get("name");

        Player player =
                playerService.updatePlayerName(userId, newName);

        return ResponseEntity.ok(Map.of(
                "message", "Updated successfully",
                "name", player.getName()
        ));
    }
}
