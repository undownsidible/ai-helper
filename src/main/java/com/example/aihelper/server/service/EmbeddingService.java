package com.example.aihelper.server.service;

import java.util.List;

public interface EmbeddingService {
    List<Float> embedding(String text);
}