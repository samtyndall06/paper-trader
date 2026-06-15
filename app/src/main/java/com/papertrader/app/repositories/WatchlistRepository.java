package com.papertrader.app.repositories;

import com.papertrader.app.models.Watchlist;
import com.papertrader.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    // Get all watchlist items for a user
    List<Watchlist> findByUser(User user);

    // Check if stock is already on watchlist
    boolean existsByUserAndSymbol(User user, String symbol);

    // Remove a stock from watchlist
    @Transactional
    void deleteByUserAndSymbol(User user, String symbol);
}