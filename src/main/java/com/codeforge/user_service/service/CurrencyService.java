package com.codeforge.user_service.service;

import com.codeforge.user_service.entity.PlayerCurrency;
import com.codeforge.user_service.repository.PlayerCurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CurrencyService {

    @Autowired
    private PlayerCurrencyRepository repo;

    public void createCurrencyIfNotExists(Long playerId) {

        Optional<PlayerCurrency> currency = repo.findById(playerId);

        if(currency.isEmpty()) {

            PlayerCurrency c = new PlayerCurrency();
            c.setPlayerId(playerId);
            c.setGold(1000); // gold khởi đầu
            c.setGem(100);    // gem khởi đầu

            repo.save(c);
        }
    }
    public PlayerCurrency getCurrency(Long playerId) {
        return repo.findById(playerId).orElseThrow();
    }

    public PlayerCurrency addGold(Long playerId, int amount) {
        PlayerCurrency c = repo.findById(playerId).orElseThrow();
        c.setGold(c.getGold() + amount);
        return repo.save(c);
    }

    public PlayerCurrency addGem(Long playerId, int amount) {
        PlayerCurrency c = repo.findById(playerId).orElseThrow();
        c.setGem(c.getGem() + amount);
        return repo.save(c);
    }

    public PlayerCurrency subtractGold(Long playerId, int amount) {

        PlayerCurrency c = repo.findById(playerId).orElseThrow();

        if(c.getGold() < amount){
            throw new RuntimeException("Not enough gold");
        }

        c.setGold(c.getGold() - amount);
        return repo.save(c);
    }

    public PlayerCurrency subtractGem(Long playerId, int amount) {

        PlayerCurrency c = repo.findById(playerId).orElseThrow();

        if(c.getGem() < amount){
            throw new RuntimeException("Not enough gem");
        }

        c.setGem(c.getGem() - amount);
        return repo.save(c);
    }
}
