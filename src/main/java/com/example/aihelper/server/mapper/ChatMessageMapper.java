package com.example.aihelper.server.mapper;

import com.example.aihelper.pojo.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    void insert(ChatMessage chatMessage);

    List<ChatMessage> listBySessionId(Long sessionId);
    List<ChatMessage> listRecentBySessionId(Long sessionId, Integer limit);

    void deleteBySessionId(Long sessionId);
}
