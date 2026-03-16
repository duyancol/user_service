package com.codeforge.user_service.controler;

import com.codeforge.user_service.entity.PlayerCurrency;
import com.codeforge.user_service.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    @Autowired
    private CurrencyService service;

    @GetMapping("/{playerId}")
    public PlayerCurrency getCurrency(@PathVariable Long playerId){
        return service.getCurrency(playerId);
    }

    @PostMapping("/addGold")
    public PlayerCurrency addGold(@RequestParam Long playerId, @RequestParam int amount){
        return service.addGold(playerId, amount);
    }

    @PostMapping("/addGem")
    public PlayerCurrency addGem(@RequestParam Long playerId, @RequestParam int amount){
        return service.addGem(playerId, amount);
    }

    @PostMapping("/subtractGold")
    public PlayerCurrency subtractGold(@RequestParam Long playerId, @RequestParam int amount){
        return service.subtractGold(playerId, amount);
    }

    @PostMapping("/subtractGem")
    public PlayerCurrency subtractGem(@RequestParam Long playerId, @RequestParam int amount){
        return service.subtractGem(playerId, amount);
    }
}
