package com.example.aihelper.server.service;

import com.example.aihelper.pojo.dto.ChatSendDTO;
import com.example.aihelper.pojo.entity.ChatMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface ChatService {
    String send(ChatSendDTO dto);

    void streamChat(ChatSendDTO dto, SseEmitter emitter);

    List<ChatMessage> listMessage(Long sessionId);
}
