package com.example.aihelper.server.service.impl;

import com.example.aihelper.server.service.FaissService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FaissServiceImpl implements FaissService {

    private static final String BASE_URL = "http://localhost:8001";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void add(List<Float> vector, Long id) {

        String url = BASE_URL + "/add";

        Map<String, Object> body = new HashMap<>();
        body.put("vectors", List.of(vector));
        body.put("ids", List.of(id));

        // ✅ 关键：设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        restTemplate.postForObject(url, request, String.class);
    }

    @Override
    public void remove(Long id) {

        String url = BASE_URL + "/remove";

        Map<String, Object> body = new HashMap<>();
        body.put("ids", List.of(id));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        restTemplate.postForObject(url, request, String.class);
    }

    @Override
    public List<Long> search(List<Float> vector, int topK) {

        String url = BASE_URL + "/search";

        Map<String, Object> body = new HashMap<>();
        body.put("vector", vector);
        body.put("top_k", topK);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        Map response = restTemplate.postForObject(url, request, Map.class);

        return (List<Long>) response.get("ids");
    }
}