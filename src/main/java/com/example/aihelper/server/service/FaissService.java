package com.example.aihelper.server.service;

import java.util.List;

public interface FaissService {

    void add(List<Float> vector, Long id);

    List<Long> search(List<Float> vector, int topK);
}