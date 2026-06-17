package com.papertrader.app.controllers;

import com.papertrader.app.services.AiSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:5173")
public class AiController {

    @Autowired
    private AiSuggestionService aiSuggestionService;

    @GetMapping("/suggestions")
    public Map<String, Object> getSuggestions(
        @RequestParam(defaultValue = "GLOBAL") String country
    ) {
        return aiSuggestionService.getSuggestions(country);
    }
}