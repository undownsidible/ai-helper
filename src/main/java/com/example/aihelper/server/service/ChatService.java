package com.example.aihelper.server.service;

import com.example.aihelper.pojo.dto.ChatSendDTO;

public interface ChatService {
    String send(ChatSendDTO dto);
}
