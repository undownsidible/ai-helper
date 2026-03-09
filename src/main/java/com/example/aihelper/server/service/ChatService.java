package com.example.aihelper.server.service;

import com.example.aihelper.pojo.dto.ChatSendDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatService {
    String send(ChatSendDTO dto);

    void streamChat(ChatSendDTO dto, SseEmitter emitter);
}
