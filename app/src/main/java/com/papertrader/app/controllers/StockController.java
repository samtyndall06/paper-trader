package com.papertrader.app.controllers;

import com.papertrader.app.services.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "http://localhost:3000")
public class StockController {

    @Autowired
    private StockService stockService;

    // Get stocks by country
    // GET /api/stocks?country=NZ
    // GET /api/stocks?country=GLOBAL
    @GetMapping
    public List<Map<String, Object>> getStocks(
        @RequestParam(defaultValue = "GLOBAL") String country
    ) {
        return stockService.getStocksByCountry(country);
    }

    // Get info for a specific stock
    // GET /api/stocks/AAPL
    @GetMapping("/{symbol}")
    public Map<String, Object> getStock(@PathVariable String symbol) {
        return stockService.getStockInfo(symbol);
    }

    // Get list of available countries
    // GET /api/stocks/countries
    @GetMapping("/countries")
    public Map<String, String> getCountries() {
        return stockService.getAvailableCountries();
    }

    // Search for a stock
    // GET /api/stocks/search?symbol=AAPL
    @GetMapping("/search")
    public Map<String, Object> searchStock(@RequestParam String symbol) {
        return stockService.searchStock(symbol);
    }
}