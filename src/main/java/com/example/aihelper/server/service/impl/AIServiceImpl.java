package com.example.aihelper.server.service.impl;

import com.example.aihelper.server.service.AIService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIServiceImpl implements AIService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String chat(String prompt) {

        String url = "http://localhost:11434/api/generate";

        Map<String, Object> body = new HashMap<>();
        body.put("model", "qwen2");
        body.put("prompt", prompt);
        body.put("stream", false);

        Map response = restTemplate.postForObject(url, body, Map.class);

        return (String) response.get("response");
    }
}
