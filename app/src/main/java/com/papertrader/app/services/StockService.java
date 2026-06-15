package com.papertrader.app.services;

import org.springframework.stereotype.Service;
import yahoofinance.YahooFinance;
import yahoofinance.Stock;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class StockService {

    // Country stock lists
    private static final Map<String, List<String>> STOCKS_BY_COUNTRY = new LinkedHashMap<>();

    static {
        STOCKS_BY_COUNTRY.put("US", Arrays.asList(
            "AAPL", "MSFT", "GOOGL", "AMZN", "TSLA",
            "NVDA", "META", "NFLX", "AMD", "JPM"
        ));
        STOCKS_BY_COUNTRY.put("NZ", Arrays.asList(
            "AIR.NZ", "FPH.NZ", "ATM.NZ", "SPK.NZ",
            "MFT.NZ", "WBC.NZ", "ANZ.NZ", "CEN.NZ",
            "MEL.NZ", "SKC.NZ"
        ));
        STOCKS_BY_COUNTRY.put("AU", Arrays.asList(
            "BHP.AX", "CBA.AX", "ANZ.AX", "WBC.AX",
            "NAB.AX", "CSL.AX", "WES.AX", "MQG.AX",
            "RIO.AX", "TLS.AX"
        ));
        STOCKS_BY_COUNTRY.put("UK", Arrays.asList(
            "SHEL.L", "AZN.L", "HSBA.L", "BP.L",
            "GSK.L", "ULVR.L", "RIO.L", "LLOY.L",
            "VOD.L", "BARC.L"
        ));
        STOCKS_BY_COUNTRY.put("JP", Arrays.asList(
            "7203.T", "6758.T", "9984.T", "7267.T",
            "6861.T", "9432.T", "8306.T", "6954.T",
            "4063.T", "9433.T"
        ));
        STOCKS_BY_COUNTRY.put("HK", Arrays.asList(
            "0700.HK", "0941.HK", "0005.HK", "1299.HK",
            "0388.HK", "2318.HK", "0939.HK", "1398.HK",
            "0883.HK", "2628.HK"
        ));
    }

    // Country display names
    private static final Map<String, String> COUNTRY_NAMES = new LinkedHashMap<>();
    static {
        COUNTRY_NAMES.put("GLOBAL", "🌍 Global");
        COUNTRY_NAMES.put("US", "🇺🇸 United States");
        COUNTRY_NAMES.put("NZ", "🇳🇿 New Zealand");
        COUNTRY_NAMES.put("AU", "🇦🇺 Australia");
        COUNTRY_NAMES.put("UK", "🇬🇧 United Kingdom");
        COUNTRY_NAMES.put("JP", "🇯🇵 Japan");
        COUNTRY_NAMES.put("HK", "🇭🇰 Hong Kong");
    }

    // Get current price of a stock
    public BigDecimal getCurrentPrice(String symbol) {
        try {
            Stock stock = YahooFinance.get(symbol);
            if (stock == null || stock.getQuote() == null) {
                return BigDecimal.ZERO;
            }
            return stock.getQuote().getPrice();
        } catch (IOException e) {
            return BigDecimal.ZERO;
        }
    }

    // Get full stock info
    public Map<String, Object> getStockInfo(String symbol) {
        Map<String, Object> info = new HashMap<>();
        try {
            Stock stock = YahooFinance.get(symbol);
            if (stock == null) {
                info.put("error", "Stock not found");
                return info;
            }
            info.put("symbol", symbol.toUpperCase());
            info.put("name", stock.getName());
            info.put("price", stock.getQuote().getPrice());
            info.put("change", stock.getQuote().getChange());
            info.put("changePercent", stock.getQuote().getChangeInPercent());
            info.put("volume", stock.getQuote().getVolume());
            info.put("yearHigh", stock.getQuote().getYearHigh());
            info.put("yearLow", stock.getQuote().getYearLow());
            info.put("currency", stock.getCurrency());
        } catch (IOException e) {
            info.put("error", "Failed to fetch stock data");
        }
        return info;
    }

    // Get multiple stocks at once
    public List<Map<String, Object>> getMultipleStocks(List<String> symbols) {
        List<Map<String, Object>> stocks = new ArrayList<>();
        for (String symbol : symbols) {
            Map<String, Object> info = getStockInfo(symbol);
            if (!info.containsKey("error")) {
                stocks.add(info);
            }
        }
        return stocks;
    }

    // Get stocks by country code
    public List<Map<String, Object>> getStocksByCountry(String countryCode) {
        if (countryCode == null || countryCode.equals("GLOBAL")) {
            return getGlobalStocks();
        }
        List<String> symbols = STOCKS_BY_COUNTRY.getOrDefault(
            countryCode.toUpperCase(), new ArrayList<>()
        );
        return getMultipleStocks(symbols);
    }

    // Get a mix of stocks from all countries
    public List<Map<String, Object>> getGlobalStocks() {
        List<String> global = new ArrayList<>();
        // Take top 2 from each country for a global mix
        for (List<String> countryStocks : STOCKS_BY_COUNTRY.values()) {
            global.addAll(countryStocks.subList(0, Math.min(2, countryStocks.size())));
        }
        return getMultipleStocks(global);
    }

    // Get available countries for the frontend
    public Map<String, String> getAvailableCountries() {
        return COUNTRY_NAMES;
    }

    // Search for a stock by symbol
    public Map<String, Object> searchStock(String symbol) {
        return getStockInfo(symbol.toUpperCase());
    }
}