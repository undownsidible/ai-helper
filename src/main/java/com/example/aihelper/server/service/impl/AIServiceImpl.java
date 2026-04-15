package com.example.aihelper.server.service.impl;

import com.example.aihelper.server.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
@Service("ollamaService")
public class AIServiceImpl implements AIService {

    private final RestTemplate restTemplate = new RestTemplate();

    // ===== 统一配置 =====
    private static final String BASE_URL = "http://localhost:11434";
    private static final String GENERATE_API = "/api/generate";
    private static final String MODEL = "qwen2";

    private String getGenerateUrl() {
        return BASE_URL + GENERATE_API;
    }

    @Override
    public String chat(String prompt) {

        String url = getGenerateUrl();

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("prompt", prompt);
        body.put("stream", false);

        Map response = restTemplate.postForObject(url, body, Map.class);

        return (String) response.get("response");
    }

    @Override
    public void streamChat(String prompt, StreamCallback callback) {

        new Thread(() -> {

            try {

                URL url = new URL(getGenerateUrl());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                ObjectMapper mapper = new ObjectMapper();

                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("model", MODEL);
                bodyMap.put("prompt", prompt);
                bodyMap.put("stream", true);

                String body = mapper.writeValueAsString(bodyMap);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
                        );

                String line;

                while ((line = reader.readLine()) != null) {

                    JsonNode json = mapper.readTree(line);

                    if (json.has("response")) {
                        callback.onMessage(json.get("response").asText());
                    }

                    if (json.has("done") && json.get("done").asBoolean()) {
                        break;
                    }
                }

                callback.onComplete();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }
}