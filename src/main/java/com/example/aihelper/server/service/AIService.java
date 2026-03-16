package com.example.aihelper.server.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AIService {
    String chat(String prompt);
    void streamChat(String prompt, StreamCallback callback);

    interface StreamCallback {

        void onMessage(String text);

        void onComplete();
    }
}
