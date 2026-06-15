package com.papertrader.app.repositories;

import com.papertrader.app.models.Trade;
import com.papertrader.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    // Get all trades for a user newest first
    List<Trade> findByUserOrderByCreatedAtDesc(User user);

    // Get trades for a specific stock
    List<Trade> findByUserAndSymbol(User user, String symbol);
}