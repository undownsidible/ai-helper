package com.example.aihelper.server.service.impl;

import com.example.aihelper.pojo.entity.ChatSession;
import com.example.aihelper.server.mapper.SessionMapper;
import com.example.aihelper.server.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private SessionMapper sessionMapper;

    @Override
    public long createSession(long userId) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle("新的聊天");
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());

        sessionMapper.insert(session);

        return session.getId();
    }

    @Override
    public List<ChatSession> listByUserId(long userId) {
        return sessionMapper.selectByUserId(userId);
    }

    @Override
    public void deleteSession(long sessionId, long userId) {
        sessionMapper.deleteByIdAndUserId(sessionId, userId);
    }
}