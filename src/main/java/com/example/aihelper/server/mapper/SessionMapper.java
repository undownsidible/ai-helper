package com.example.aihelper.server.mapper;

import com.example.aihelper.pojo.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SessionMapper {

    void insert(ChatSession session);

    List<ChatSession> selectByUserId(Long userId);

    void deleteByIdAndUserId(@Param("sessionId") Long sessionId,
                             @Param("userId") Long userId);
}