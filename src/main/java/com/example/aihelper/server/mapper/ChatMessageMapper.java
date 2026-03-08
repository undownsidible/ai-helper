package com.example.aihelper.server.mapper;

import com.example.aihelper.pojo.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    void insert(ChatMessage chatMessage);

    List<ChatMessage> listBySessionId(Long sessionId);

    void deleteBySessionId(Long sessionId);
}
