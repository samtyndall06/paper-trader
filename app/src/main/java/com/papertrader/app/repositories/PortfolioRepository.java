package com.papertrader.app.repositories;

import com.papertrader.app.models.Portfolio;
import com.papertrader.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    // Get all stocks a user owns
    List<Portfolio> findByUser(User user);

    // Get a specific stock a user owns
    Optional<Portfolio> findByUserAndSymbol(User user, String symbol);

    // Check if user already owns a stock
    boolean existsByUserAndSymbol(User user, String symbol);
}