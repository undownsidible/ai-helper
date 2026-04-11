package com.example.aihelper.server.service.impl;

import com.example.aihelper.server.service.EmbeddingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final String URL = "http://localhost:11434/api/embeddings";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<Float> embedding(String text) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", "nomic-embed-text");
            body.put("prompt", text);

            String response = restTemplate.postForObject(URL, body, String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode embeddingNode = root.get("embedding");

            List<Float> result = new ArrayList<>();
            for (JsonNode node : embeddingNode) {
                result.add((float) node.asDouble());
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("embedding失败", e);
        }
    }
}