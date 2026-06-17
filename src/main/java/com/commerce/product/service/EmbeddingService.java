package com.commerce.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String OPENAI_EMBEDDINGS_URL = "https://api.openai.com/v1/embeddings";
    private static final String MODEL = "text-embedding-3-small";

    @SuppressWarnings("unchecked")
    public List<Double> embed(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.exchange(
                OPENAI_EMBEDDINGS_URL, HttpMethod.POST,
                new HttpEntity<>(Map.of("input", text, "model", MODEL), headers),
                Map.class);

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
        return (List<Double>) data.get(0).get("embedding");
    }

    public String buildProductText(String name, String description, List<String> categories, String brand) {
        StringBuilder sb = new StringBuilder();
        if (name != null)        sb.append(name).append(". ");
        if (description != null) sb.append(description).append(". ");
        if (categories != null && !categories.isEmpty()) sb.append(String.join(", ", categories)).append(". ");
        if (brand != null)       sb.append(brand);
        return sb.toString().trim();
    }
}
