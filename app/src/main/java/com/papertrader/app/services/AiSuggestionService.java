package com.papertrader.app.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class AiSuggestionService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = createRestTemplate();

    private RestTemplate createRestTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory
                = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 5 seconds to connect
        factory.setReadTimeout(60000);    // 60 seconds to get a response
        return new RestTemplate(factory);
    }
    
    @Autowired
    private StockService stockService;

    public Map<String, Object> getSuggestions(String country) {
        try {
            List<Map<String, Object>> stocks = stockService.getStocksByCountry(country);

            if (stocks.isEmpty()) {
                return getMockSuggestions(country);
            }

            StringBuilder stockSummary = new StringBuilder();
            for (Map<String, Object> stock : stocks) {
                stockSummary.append(stock.get("symbol"))
                        .append(": $").append(stock.get("price"))
                        .append(" (").append(stock.get("changePercent")).append("% today)\n");
            }

            List<Map<String, Object>> allSuggestions = new ArrayList<>();
            Set<String> usedSymbols = new HashSet<>();

            // Ask Ollama 3 times for 1 suggestion each — more reliable than asking for 3 at once
            for (int i = 0; i < 3; i++) {
                String exclude = usedSymbols.isEmpty() ? ""
                        : "\nDo not suggest these symbols again: " + String.join(", ", usedSymbols);

                String prompt = "Based on this stock data:\n\n"
                        + stockSummary.toString()
                        + exclude
                        + "\nPick exactly ONE stock to suggest buying. "
                        + "Respond with ONLY this JSON object, nothing else:\n"
                        + "{\"symbol\": \"TICKER\", \"reason\": \"short reason\", \"confidence\": \"High or Medium or Low\"}";

                Map<String, Object> body = new HashMap<>();
                body.put("model", "llama3.2");
                body.put("prompt", prompt);
                body.put("stream", false);
                body.put("format", "json");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

                String response = restTemplate.postForObject(
                        "http://localhost:11434/api/generate", request, String.class
                );

                JsonNode root = objectMapper.readTree(response);
                String text = root.path("response").asText().trim();

                JsonNode suggestion = objectMapper.readTree(text);

                if (suggestion.has("symbol")) {
                    Map<String, Object> s = new HashMap<>();
                    s.put("symbol", suggestion.get("symbol").asText());
                    s.put("reason", suggestion.path("reason").asText("No reason given"));
                    s.put("confidence", suggestion.path("confidence").asText("Medium"));
                    allSuggestions.add(s);
                    usedSymbols.add(suggestion.get("symbol").asText());
                }
            }

            if (allSuggestions.isEmpty()) {
                return getMockSuggestions(country);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("suggestions", allSuggestions);
            result.put("generatedAt", new Date());
            return result;

        } catch (Exception e) {
            System.out.println("AI suggestion error: " + e.getMessage());
            return getMockSuggestions(country);
        }
    }

    private Map<String, Object> getMockSuggestions(String country) {
        List<Map<String, Object>> mockSuggestions = new ArrayList<>();

        Map<String, Object> s1 = new HashMap<>();
        s1.put("symbol", "NVDA");
        s1.put("reason", "Strong momentum in AI sector continues to drive growth");
        s1.put("confidence", "High");
        mockSuggestions.add(s1);

        Map<String, Object> s2 = new HashMap<>();
        s2.put("symbol", "MSFT");
        s2.put("reason", "Consistent cloud revenue growth and stable fundamentals");
        s2.put("confidence", "Medium");
        mockSuggestions.add(s2);

        Map<String, Object> s3 = new HashMap<>();
        s3.put("symbol", "AAPL");
        s3.put("reason", "Strong brand loyalty and steady product cycle");
        s3.put("confidence", "Medium");
        mockSuggestions.add(s3);

        Map<String, Object> result = new HashMap<>();
        result.put("suggestions", mockSuggestions);
        result.put("generatedAt", new Date());
        result.put("note", "Mock data - Ollama unavailable");
        return result;
    }
}