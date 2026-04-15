package com.example.aihelper.server.service;

import com.example.aihelper.pojo.dto.SessionUpdateDTO;
import com.example.aihelper.pojo.entity.ChatSession;

import java.util.List;

public interface SessionService {
    long createSession(long userId);
    List<ChatSession> listByUserId(long userId);
    void deleteSession(long sessionId, long userId);
    void updateSessionName(SessionUpdateDTO dto, Long userId);
}
