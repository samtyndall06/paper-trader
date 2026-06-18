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
        COUNTRY_NAMES.put("GLOBAL", "Global");
        COUNTRY_NAMES.put("US", "United States");
        COUNTRY_NAMES.put("NZ", "New Zealand");
        COUNTRY_NAMES.put("AU", "Australia");
        COUNTRY_NAMES.put("UK", "United Kingdom");
        COUNTRY_NAMES.put("JP", "Japan");
        COUNTRY_NAMES.put("HK", "Hong Kong");
    }

    // Get current price using Alpha Vantage
    public BigDecimal getCurrentPrice(String symbol) {
        try {
            String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
                    + symbol + "&apikey=" + apiKey;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode quote = root.path("Global Quote");
            if (quote.has("05. price") && !quote.get("05. price").asText().isEmpty()) {
                return new BigDecimal(quote.get("05. price").asText());
            }
        } catch (Exception e) {
            System.out.println("Error fetching price for " + symbol + ": " + e.getMessage());
        }

        // Fall back to mock price if API fails
        Map<String, Object> mockStock = getMockStockBySymbol(symbol);
        if (mockStock != null) {
            return BigDecimal.valueOf((double) mockStock.get("price"));
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
        String code = (countryCode == null || countryCode.equals("GLOBAL"))
                ? "GLOBAL" : countryCode.toUpperCase();

        // Try real API first
        List<Map<String, Object>> realData = code.equals("GLOBAL")
                ? getGlobalStocks()
                : getMultipleStocks(STOCKS_BY_COUNTRY.getOrDefault(code, new ArrayList<>()));

        // Fall back to mock data if API returns nothing
        if (realData.isEmpty()) {
            System.out.println("API limit reached - using mock data for: " + code);
            return getMockStocks(code);
        }
        return realData;
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
    
    // Mock data for development when API limit is reached
    private List<Map<String, Object>> getMockStocks(String country) {
        List<Map<String, Object>> stocks = new ArrayList<>();

        Map<String, List<Map<String, Object>>> mockData = new HashMap<>();

        mockData.put("US", Arrays.asList(
                createMock("AAPL", "Apple Inc.", 189.50, 1.25, 0.66, "USD"),
                createMock("MSFT", "Microsoft Corp.", 415.20, -2.30, -0.55, "USD"),
                createMock("GOOGL", "Alphabet Inc.", 175.80, 3.10, 1.79, "USD"),
                createMock("TSLA", "Tesla Inc.", 245.60, -5.40, -2.15, "USD"),
                createMock("NVDA", "NVIDIA Corp.", 875.30, 12.50, 1.45, "USD")
        ));
        mockData.put("NZ", Arrays.asList(
                createMock("AIR.NZ", "Air New Zealand", 0.68, 0.01, 1.49, "NZD"),
                createMock("FPH.NZ", "Fisher & Paykel", 22.50, -0.30, -1.32, "NZD"),
                createMock("ATM.NZ", "A2 Milk Company", 6.85, 0.15, 2.24, "NZD"),
                createMock("SPK.NZ", "Spark NZ", 3.12, -0.05, -1.58, "NZD"),
                createMock("MFT.NZ", "Mainfreight", 68.40, 0.90, 1.33, "NZD")
        ));
        mockData.put("AU", Arrays.asList(
                createMock("BHP.AX", "BHP Group", 45.20, 0.80, 1.80, "AUD"),
                createMock("CBA.AX", "Commonwealth Bank", 118.50, -1.20, -1.00, "AUD"),
                createMock("ANZ.AX", "ANZ Banking Group", 28.90, 0.45, 1.58, "AUD"),
                createMock("WBC.AX", "Westpac Banking", 26.75, -0.25, -0.93, "AUD"),
                createMock("NAB.AX", "National Aust. Bank", 33.40, 0.60, 1.83, "AUD")
        ));
        mockData.put("UK", Arrays.asList(
                createMock("SHEL.L", "Shell PLC", 2456.00, 12.00, 0.49, "GBP"),
                createMock("AZN.L", "AstraZeneca", 12850.00, -50.00, -0.39, "GBP"),
                createMock("HSBA.L", "HSBC Holdings", 745.60, 8.40, 1.14, "GBP"),
                createMock("BP.L", "BP PLC", 425.30, -3.20, -0.75, "GBP"),
                createMock("GSK.L", "GSK PLC", 1685.00, 15.00, 0.90, "GBP")
        ));
        mockData.put("JP", Arrays.asList(
                createMock("7203.T", "Toyota Motor", 3250.00, 45.00, 1.40, "JPY"),
                createMock("6758.T", "Sony Group", 12500.00, -150.00, -1.19, "JPY"),
                createMock("9984.T", "SoftBank Group", 8750.00, 120.00, 1.39, "JPY"),
                createMock("7267.T", "Honda Motor", 1456.00, 23.00, 1.61, "JPY"),
                createMock("6861.T", "Keyence Corp.", 65800.00, -800.00, -1.20, "JPY")
        ));
        mockData.put("HK", Arrays.asList(
                createMock("0700.HK", "Tencent Holdings", 385.60, 4.20, 1.10, "HKD"),
                createMock("0941.HK", "China Mobile", 68.45, -0.55, -0.80, "HKD"),
                createMock("0005.HK", "HSBC Holdings HK", 72.30, 0.85, 1.19, "HKD"),
                createMock("1299.HK", "AIA Group", 58.90, -0.40, -0.67, "HKD"),
                createMock("0388.HK", "HK Exchanges", 285.40, 3.60, 1.28, "HKD")
        ));

        if (country.equals("GLOBAL")) {
            for (List<Map<String, Object>> countryStocks : mockData.values()) {
                stocks.add(countryStocks.get(0));
            }
        } else {
            stocks = mockData.getOrDefault(country, new ArrayList<>());
        }
        return stocks;
    }

    private Map<String, Object> createMock(String symbol, String name, double price,
            double change, double changePercent, String currency) {
        Map<String, Object> stock = new HashMap<>();
        stock.put("symbol", symbol);
        stock.put("name", name);
        stock.put("price", price);
        stock.put("change", change);
        stock.put("changePercent", changePercent);
        stock.put("currency", currency);
        stock.put("volume", (long) (Math.random() * 10000000));
        return stock;
    }
    
    public Map<String, Object> getMockStockBySymbol(String symbol) {
        // Search through all mock data for the symbol
        for (List<Map<String, Object>> countryStocks : getMockStocks("GLOBAL").stream()
                .map(s -> List.of(s)).collect(java.util.stream.Collectors.toList())) {
            for (Map<String, Object> stock : countryStocks) {
                if (stock.get("symbol").equals(symbol.toUpperCase())) {
                    return stock;
                }
            }
        }
        // Build from all country mock data
        List<String> allCountries = Arrays.asList("US", "NZ", "AU", "UK", "JP", "HK");
        for (String country : allCountries) {
            for (Map<String, Object> stock : getMockStocks(country)) {
                if (stock.get("symbol").equals(symbol.toUpperCase())) {
                    return stock;
                }
            }
        }
        return null;
    }
    
    public List<Map<String, Object>> getMockHistoricalData(String symbol) {
        List<Map<String, Object>> history = new ArrayList<>();
        Random random = new Random(symbol.hashCode());

        // Get current price from mock data
        Map<String, Object> stock = getMockStockBySymbol(symbol);
        double basePrice = stock != null ? (double) stock.get("price") : 100.0;

        // Generate 30 days of historical data
        double price = basePrice * 0.85;
        for (int i = 29; i >= 0; i--) {
            Map<String, Object> day = new HashMap<>();
            // Random walk up toward current price
            price = price + (random.nextGaussian() * basePrice * 0.02)
                    + (basePrice - price) * 0.05;

            java.time.LocalDate date = java.time.LocalDate.now().minusDays(i);
            day.put("date", date.toString());
            day.put("price", Math.round(price * 100.0) / 100.0);
            day.put("open", Math.round((price * 0.99) * 100.0) / 100.0);
            day.put("high", Math.round((price * 1.02) * 100.0) / 100.0);
            day.put("low", Math.round((price * 0.98) * 100.0) / 100.0);
            history.add(day);
        }
        return history;
    }
}