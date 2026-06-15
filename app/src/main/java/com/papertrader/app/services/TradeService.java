package com.papertrader.app.services;

import com.papertrader.app.models.*;
import com.papertrader.app.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class TradeService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockService stockService;

    // Buy a stock
    @Transactional
    public Trade buyStock(User user, String symbol, BigDecimal shares) {
        BigDecimal price = stockService.getCurrentPrice(symbol);
        BigDecimal total = price.multiply(shares).setScale(2, RoundingMode.HALF_UP);

        // Check user has enough balance
        if (user.getBalance().compareTo(total) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Deduct from balance
        user.setBalance(user.getBalance().subtract(total));
        userRepository.save(user);

        // Update portfolio
        Optional<Portfolio> existing = portfolioRepository.findByUserAndSymbol(user, symbol);
        if (existing.isPresent()) {
            // Already owns this stock — update average price and shares
            Portfolio portfolio = existing.get();
            BigDecimal totalShares = portfolio.getShares().add(shares);
            BigDecimal totalCost = portfolio.getAvgBuyPrice()
                .multiply(portfolio.getShares())
                .add(price.multiply(shares));
            BigDecimal newAvg = totalCost.divide(totalShares, 2, RoundingMode.HALF_UP);
            portfolio.setShares(totalShares);
            portfolio.setAvgBuyPrice(newAvg);
            portfolioRepository.save(portfolio);
        } else {
            // First time buying this stock
            Portfolio portfolio = new Portfolio();
            portfolio.setUser(user);
            portfolio.setSymbol(symbol.toUpperCase());
            portfolio.setShares(shares);
            portfolio.setAvgBuyPrice(price);
            portfolioRepository.save(portfolio);
        }

        // Record the trade
        Trade trade = new Trade();
        trade.setUser(user);
        trade.setSymbol(symbol.toUpperCase());
        trade.setType("BUY");
        trade.setShares(shares);
        trade.setPrice(price);
        trade.setTotal(total);
        return tradeRepository.save(trade);
    }

    // Sell a stock
    @Transactional
    public Trade sellStock(User user, String symbol, BigDecimal shares) {
        Portfolio portfolio = portfolioRepository.findByUserAndSymbol(user, symbol)
            .orElseThrow(() -> new RuntimeException("You don't own this stock"));

        // Check user has enough shares
        if (portfolio.getShares().compareTo(shares) < 0) {
            throw new RuntimeException("Insufficient shares");
        }

        BigDecimal price = stockService.getCurrentPrice(symbol);
        BigDecimal total = price.multiply(shares).setScale(2, RoundingMode.HALF_UP);

        // Add to balance
        user.setBalance(user.getBalance().add(total));
        userRepository.save(user);

        // Update portfolio
        BigDecimal remainingShares = portfolio.getShares().subtract(shares);
        if (remainingShares.compareTo(BigDecimal.ZERO) == 0) {
            // Sold all shares — remove from portfolio
            portfolioRepository.delete(portfolio);
        } else {
            portfolio.setShares(remainingShares);
            portfolioRepository.save(portfolio);
        }

        // Record the trade
        Trade trade = new Trade();
        trade.setUser(user);
        trade.setSymbol(symbol.toUpperCase());
        trade.setType("SELL");
        trade.setShares(shares);
        trade.setPrice(price);
        trade.setTotal(total);
        return tradeRepository.save(trade);
    }

    // Get trade history for a user
    public List<Trade> getTradeHistory(User user) {
        return tradeRepository.findByUserOrderByCreatedAtDesc(user);
    }
}