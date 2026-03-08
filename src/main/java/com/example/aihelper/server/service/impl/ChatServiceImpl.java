package com.example.aihelper.server.service.impl;

import com.example.aihelper.pojo.dto.ChatSendDTO;
import com.example.aihelper.pojo.entity.ChatMessage;
import com.example.aihelper.server.mapper.ChatMessageMapper;
import com.example.aihelper.server.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    ChatMessageMapper chatMessageMapper;
    @Override
    public String send(ChatSendDTO dto) {

        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(dto.getSessionId());
        userMessage.setRole("user");
        userMessage.setContent(dto.getContent());

        chatMessageMapper.insert(userMessage);

        String reply = "AI回复示例";

        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setSessionId(dto.getSessionId());
        aiMessage.setRole("assistant");
        aiMessage.setContent(reply);

        chatMessageMapper.insert(aiMessage);

        return reply;
    }
}
