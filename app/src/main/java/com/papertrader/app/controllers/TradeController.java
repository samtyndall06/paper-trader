package com.papertrader.app.controllers;

import com.papertrader.app.models.Trade;
import com.papertrader.app.models.User;
import com.papertrader.app.services.TradeService;
import com.papertrader.app.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/trades")
@CrossOrigin(origins = "http://localhost:5173")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private UserService userService;

    @PostMapping("/buy")
    public ResponseEntity<?> buyStock(@RequestBody Map<String, Object> body) {
        try {
            User user = userService.getUserByEmail((String) body.get("email"));
            String symbol = (String) body.get("symbol");
            BigDecimal shares = new BigDecimal(body.get("shares").toString());
            Trade trade = tradeService.buyStock(user, symbol, shares);
            return ResponseEntity.ok(Map.of(
                "message", "Buy order placed successfully",
                "symbol", trade.getSymbol(),
                "shares", trade.getShares(),
                "price", trade.getPrice(),
                "total", trade.getTotal(),
                "newBalance", user.getBalance()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellStock(@RequestBody Map<String, Object> body) {
        try {
            User user = userService.getUserByEmail((String) body.get("email"));
            String symbol = (String) body.get("symbol");
            BigDecimal shares = new BigDecimal(body.get("shares").toString());
            Trade trade = tradeService.sellStock(user, symbol, shares);
            return ResponseEntity.ok(Map.of(
                "message", "Sell order placed successfully",
                "symbol", trade.getSymbol(),
                "shares", trade.getShares(),
                "price", trade.getPrice(),
                "total", trade.getTotal(),
                "newBalance", user.getBalance()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestParam String email) {
        try {
            User user = userService.getUserByEmail(email);
            List<Trade> trades = tradeService.getTradeHistory(user);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Trade trade : trades) {
                Map<String, Object> t = new HashMap<>();
                t.put("id", trade.getId());
                t.put("symbol", trade.getSymbol());
                t.put("type", trade.getType());
                t.put("shares", trade.getShares());
                t.put("price", trade.getPrice());
                t.put("total", trade.getTotal());
                t.put("createdAt", trade.getCreatedAt());
                result.add(t);
            }
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}