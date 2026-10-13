package com.ccd.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class SummarizerService {
    private static final Logger log = LoggerFactory.getLogger(SummarizerService.class);
    private static final List<String> FALLBACK_MODELS =
            List.of("gemini-3.5-flash-lite", "gemini-3.1-flash-lite");

    private final String apiKey;
    private final String model;
        private final RestClient http = RestClient.builder()
            .requestFactory(timeoutFactory())
            .build();

    private static SimpleClientHttpRequestFactory timeoutFactory() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(5000);   // give up connecting after 5 seconds
        f.setReadTimeout(20000);     // give up waiting for an answer after 20 seconds
        return f;
    }

    public SummarizerService(@Value("${gemini.api-key:}") String apiKey,
                             @Value("${gemini.model:gemini-3.8-flash}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    public String summarize(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY not set, returning mock summary");
            return mock(text);
        }
        String prompt = "Summarize the following note in 2-3 short sentences, "
                + "then list up to 3 key highlights as bullet points:\n\n" + text;
        Map<String, Object> body = Map.of("contents",
                List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

        List<String> models = java.util.stream.Stream
                .concat(java.util.stream.Stream.of(model), FALLBACK_MODELS.stream())
                .distinct()
                .toList();

        RestClientException last = null;
        for (String m : models) {
            try {
                String out = call(m, body);
                log.info("Generated summary with {} ({} chars)", m, out.length());
                return out;
            } catch (RestClientException e) {
                log.warn("Model {} failed, trying next: {}", m, e.getMessage());
                last = e;
            }
        }
        throw last;
    }

    private String call(String modelName, Map<String, Object> body) {
        JsonNode res = http.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent")
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        return res == null ? "" : res.path("candidates").path(0).path("content")
                .path("parts").path(0).path("text").asText("");
    }

    String mock(String text) {
        String t = text.length() <= 200 ? text : text.substring(0, 200) + "...";
        return "[mock summary] " + t;
    }
}
