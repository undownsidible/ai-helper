package com.example.aihelper.server.service.impl;

import com.example.aihelper.pojo.dto.ChatSendDTO;
import com.example.aihelper.pojo.entity.ChatMessage;
import com.example.aihelper.server.mapper.ChatMessageMapper;
import com.example.aihelper.server.service.AIService;
import com.example.aihelper.server.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private AIService aiService;


    @Override
    public String send(ChatSendDTO dto) {
        // 保存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(dto.getSessionId());
        userMsg.setRole("user");
        userMsg.setContent(dto.getContent());

        chatMessageMapper.insert(userMsg);

        // 调用 AI
        String reply = aiService.chat(dto.getContent());

        // 保存 AI 回复
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(dto.getSessionId());
        aiMsg.setRole("assistant");
        aiMsg.setContent(reply);

        chatMessageMapper.insert(aiMsg);

        return reply;
    }
}