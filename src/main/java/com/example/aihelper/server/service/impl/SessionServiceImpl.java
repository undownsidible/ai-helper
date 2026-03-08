package com.example.aihelper.server.service.impl;

import com.example.aihelper.common.constant.MessageConstant;
import com.example.aihelper.common.exception.NoRightException;
import com.example.aihelper.common.exception.NotLoginException;
import com.example.aihelper.common.exception.SessionNotFoundException;
import com.example.aihelper.pojo.entity.ChatMessage;
import com.example.aihelper.pojo.entity.ChatSession;
import com.example.aihelper.server.mapper.ChatMessageMapper;
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
    @Autowired
    private ChatMessageMapper chatMessageMapper;

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

        // 1 查询 session
        ChatSession session = sessionMapper.getById(sessionId);

        if (session == null) {
            throw new SessionNotFoundException(MessageConstant.SESSION_NOT_FOUND);
        }

        // 2 判断是否属于当前用户
        if (!session.getUserId().equals(userId)) {
            throw new NoRightException(MessageConstant.NO_RIGHT);
        }

        // 3 删除聊天记录
        chatMessageMapper.deleteBySessionId(sessionId);

        // 4 删除会话
        sessionMapper.deleteById(sessionId);
    }
}