package com.example.aihelper.server.service.impl;

import com.example.aihelper.server.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("apiService")
public class ApiAIServiceImpl implements AIService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ====================== 阿里云百炼统一配置（只改这里）======================
    // 阿里云兼容 OpenAI 格式接口地址
    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    // 你的阿里云百炼 API Key
    private static final String API_KEY = "sk-a1ad3b6e49ff4f50ae7eb7a06f2a2f33";
    // 模型名称（超便宜好用：qwen3.5-flash）
    private static final String MODEL_NAME = "qwen3.5-flash";
    // ========================================================================

    @Override
    public String chat(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL_NAME); // 使用统一变量
        body.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        Map res = restTemplate.postForObject(API_URL, request, Map.class);

        List choices = (List) res.get("choices");
        Map msg = (Map) ((Map) choices.get(0)).get("message");

        return (String) msg.get("content");
    }

    @Override
    public void streamChat(String prompt, StreamCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("model", MODEL_NAME); // 使用统一变量
                bodyMap.put("stream", true);
                bodyMap.put("messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ));

                String body = objectMapper.writeValueAsString(bodyMap);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
                );

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) continue;

                    String data = line.substring(5).trim();
                    if ("[DONE]".equals(data)) break;

                    JsonNode json = objectMapper.readTree(data);
                    JsonNode delta = json.at("/choices/0/delta/content");
                    if (!delta.isNull()) {
                        callback.onMessage(delta.asText());
                    }
                }

                callback.onComplete();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}