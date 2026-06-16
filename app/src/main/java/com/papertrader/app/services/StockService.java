package com.papertrader.app.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.*;

@Service
public class StockService {

    @Value("${alpha.vantage.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Country stock lists
    private static final Map<String, List<String>> STOCKS_BY_COUNTRY = new LinkedHashMap<>();
    static {
        STOCKS_BY_COUNTRY.put("US", Arrays.asList(
            "AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "NVDA", "META", "NFLX", "AMD", "JPM"
        ));
        STOCKS_BY_COUNTRY.put("NZ", Arrays.asList(
            "AIR.NZ", "FPH.NZ", "ATM.NZ", "SPK.NZ", "MFT.NZ"
        ));
        STOCKS_BY_COUNTRY.put("AU", Arrays.asList(
            "BHP.AX", "CBA.AX", "ANZ.AX", "WBC.AX", "NAB.AX"
        ));
        STOCKS_BY_COUNTRY.put("UK", Arrays.asList(
            "SHEL.L", "AZN.L", "HSBA.L", "BP.L", "GSK.L"
        ));
        STOCKS_BY_COUNTRY.put("JP", Arrays.asList(
            "7203.T", "6758.T", "9984.T", "7267.T", "6861.T"
        ));
        STOCKS_BY_COUNTRY.put("HK", Arrays.asList(
            "0700.HK", "0941.HK", "0005.HK", "1299.HK", "0388.HK"
        ));
    }

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

    // Get current price using Alpha Vantage
    public BigDecimal getCurrentPrice(String symbol) {
        try {
            String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
                + symbol + "&apikey=" + apiKey;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode quote = root.path("Global Quote");
            if (quote.has("05. price")) {
                return new BigDecimal(quote.get("05. price").asText());
            }
        } catch (Exception e) {
            System.out.println("Error fetching price for " + symbol + ": " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    // Get full stock info
    public Map<String, Object> getStockInfo(String symbol) {
        Map<String, Object> info = new HashMap<>();
        try {
            System.out.println("Fetching: " + symbol);
            String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
                + symbol + "&apikey=" + apiKey;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode quote = root.path("Global Quote");

            if (!quote.has("05. price") || quote.get("05. price").asText().isEmpty()) {
                System.out.println("No data for: " + symbol);
                return null;
            }

            double price = Double.parseDouble(quote.get("05. price").asText());
            double change = Double.parseDouble(quote.get("09. change").asText());
            double changePercent = Double.parseDouble(
                quote.get("10. change percent").asText().replace("%", "")
            );
            long volume = Long.parseLong(quote.get("06. volume").asText());

            info.put("symbol", symbol.toUpperCase());
            info.put("name", symbol);
            info.put("price", price);
            info.put("change", change);
            info.put("changePercent", changePercent);
            info.put("volume", volume);
            info.put("currency", getCurrency(symbol));

            System.out.println("Got " + symbol + " at " + price);

        } catch (Exception e) {
            System.out.println("Error for " + symbol + ": " + e.getMessage());
            return null;
        }
        return info;
    }

    // Determine currency from symbol suffix
    private String getCurrency(String symbol) {
        if (symbol.endsWith(".NZ")) return "NZD";
        if (symbol.endsWith(".AX")) return "AUD";
        if (symbol.endsWith(".L")) return "GBP";
        if (symbol.endsWith(".T")) return "JPY";
        if (symbol.endsWith(".HK")) return "HKD";
        return "USD";
    }

    // Get multiple stocks
    public List<Map<String, Object>> getMultipleStocks(List<String> symbols) {
        List<Map<String, Object>> stocks = new ArrayList<>();
        for (String symbol : symbols) {
            Map<String, Object> info = getStockInfo(symbol);
            if (info != null) {
                stocks.add(info);
            }
            // Alpha Vantage free tier allows 25 requests/day and 5/minute
            // Add a small delay between requests
            try { Thread.sleep(1000); } catch (InterruptedException e) { }
        }
        return stocks;
    }

    // Get stocks by country
    public List<Map<String, Object>> getStocksByCountry(String countryCode) {
        if (countryCode == null || countryCode.equals("GLOBAL")) {
            return getGlobalStocks();
        }
        List<String> symbols = STOCKS_BY_COUNTRY.getOrDefault(
            countryCode.toUpperCase(), new ArrayList<>()
        );
        return getMultipleStocks(symbols);
    }

    // Get global mix — 1 stock per country to stay within rate limits
    public List<Map<String, Object>> getGlobalStocks() {
        List<String> global = new ArrayList<>();
        for (List<String> countryStocks : STOCKS_BY_COUNTRY.values()) {
            if (!countryStocks.isEmpty()) {
                global.add(countryStocks.get(0));
            }
        }
        return getMultipleStocks(global);
    }

    public Map<String, String> getAvailableCountries() {
        return COUNTRY_NAMES;
    }

    public Map<String, Object> searchStock(String symbol) {
        return getStockInfo(symbol.toUpperCase());
    }
}