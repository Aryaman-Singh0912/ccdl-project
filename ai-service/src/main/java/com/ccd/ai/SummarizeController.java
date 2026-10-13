package com.ccd.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class SummarizeController {
    private static final Logger log = LoggerFactory.getLogger(SummarizeController.class);
    private final SummarizerService service;

    public SummarizeController(SummarizerService service) {
        this.service = service;
    }

    public record SummarizeRequest(String text) {}

    @PostMapping("/summarize")
    public Map<String, String> summarize(@RequestBody SummarizeRequest req) {
        if (req == null || req.text() == null || req.text().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "text must not be empty");
        }
        try {
            return Map.of("summary", service.summarize(req.text()));
        } catch (RestClientException e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream AI API failed");
        }
    }
}
