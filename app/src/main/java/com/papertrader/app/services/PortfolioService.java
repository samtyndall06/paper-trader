package com.papertrader.app.services;

import com.papertrader.app.models.Portfolio;
import com.papertrader.app.models.User;
import com.papertrader.app.repositories.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private StockService stockService;

    // Get portfolio with current prices and gains/losses
    public List<Map<String, Object>> getPortfolioWithPrices(User user) {
        List<Portfolio> holdings = portfolioRepository.findByUser(user);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Portfolio holding : holdings) {
            Map<String, Object> item = new HashMap<>();
            BigDecimal currentPrice = stockService.getCurrentPrice(holding.getSymbol());
            BigDecimal currentValue = currentPrice.multiply(holding.getShares())
                .setScale(2, RoundingMode.HALF_UP);
            BigDecimal costBasis = holding.getAvgBuyPrice().multiply(holding.getShares())
                .setScale(2, RoundingMode.HALF_UP);
            BigDecimal gainLoss = currentValue.subtract(costBasis)
                .setScale(2, RoundingMode.HALF_UP);
            BigDecimal gainLossPercent = costBasis.compareTo(BigDecimal.ZERO) > 0
                ? gainLoss.divide(costBasis, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            item.put("symbol", holding.getSymbol());
            item.put("shares", holding.getShares());
            item.put("avgBuyPrice", holding.getAvgBuyPrice());
            item.put("currentPrice", currentPrice);
            item.put("currentValue", currentValue);
            item.put("gainLoss", gainLoss);
            item.put("gainLossPercent", gainLossPercent);
            result.add(item);
        }
        return result;
    }

    // Get total portfolio value
    public BigDecimal getTotalPortfolioValue(User user) {
        return getPortfolioWithPrices(user).stream()
            .map(item -> (BigDecimal) item.get("currentValue"))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}