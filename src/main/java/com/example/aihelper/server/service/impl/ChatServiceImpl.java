package com.example.aihelper.server.service.impl;

import com.example.aihelper.common.constant.MessageConstant;
import com.example.aihelper.common.context.UserContext;
import com.example.aihelper.common.exception.SessionNotFoundException;
import com.example.aihelper.pojo.dto.ChatSendDTO;
import com.example.aihelper.pojo.entity.ChatMessage;
import com.example.aihelper.pojo.entity.Schedule;
import com.example.aihelper.server.mapper.ChatMessageMapper;
import com.example.aihelper.server.mapper.SessionMapper;
import com.example.aihelper.server.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {



    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private AIService aiService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private FaissService faissService;

    @Autowired
    private ScheduleService scheduleService;

    private static final int MAX_HISTORY = 10;


    @Override
    public String send(ChatSendDTO dto) {

        // 1. session校验
        if (sessionMapper.getById(dto.getSessionId()) == null) {
            throw new RuntimeException("session不存在");
        }

        // 2. 存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(dto.getSessionId());
        userMsg.setRole("user");
        userMsg.setContent(dto.getContent());
        chatMessageMapper.insert(userMsg);

        // 3. ===== RAG部分（核心新增）=====
        Long userId = UserContext.getUserId();
        String context = buildRagContext(userId, dto.getContent());
        // 4. 历史消息
        List<ChatMessage> history =
                chatMessageMapper.listRecentBySessionId(dto.getSessionId(), MAX_HISTORY);

        Collections.reverse(history);

        StringBuilder promptBuilder = new StringBuilder();
        // 👉 RAG上下文
        promptBuilder.append("你是日程助手。相关日程：\n")
                .append(context)
                .append("\n");

        for (ChatMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                promptBuilder.append("用户：").append(msg.getContent()).append("\n");
            } else {
                promptBuilder.append("助手：").append(msg.getContent()).append("\n");
            }
        }

        //promptBuilder.append("用户：").append(dto.getContent()).append("\n");
        promptBuilder.append("助手：");

        String prompt = promptBuilder.toString();

        log.info("prompt:\n{}", prompt);
        // 5. 调AI
        String reply = aiService.chat(prompt);

        // 6. 存AI回复
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(dto.getSessionId());
        aiMsg.setRole("assistant");
        aiMsg.setContent(reply);
        chatMessageMapper.insert(aiMsg);

        return reply;
    }
    @Override
    public void streamChat(ChatSendDTO dto, SseEmitter emitter) {

        // 1. session校验
        if (sessionMapper.getById(dto.getSessionId()) == null) {
            throw new RuntimeException("session不存在");
        }

        // 2. 存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(dto.getSessionId());
        userMsg.setRole("user");
        userMsg.setContent(dto.getContent());
        chatMessageMapper.insert(userMsg);

        // 3. ===== RAG部分 =====
        Long userId = UserContext.getUserId();
        String context = buildRagContext(userId, dto.getContent());

        // 4. 查历史消息
        List<ChatMessage> history =
                chatMessageMapper.listRecentBySessionId(dto.getSessionId(), MAX_HISTORY);

        Collections.reverse(history);

        // 5. 拼prompt
        StringBuilder promptBuilder = new StringBuilder();

        // 👉 RAG上下文
        promptBuilder.append("你是日程助手。相关日程：\n").append(context).append("\n");

        // 👉 历史对话
        for (ChatMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                promptBuilder.append("用户：").append(msg.getContent()).append("\n");
            } else {
                promptBuilder.append("助手：").append(msg.getContent()).append("\n");
            }
        }

        // 👉 当前问题
        //promptBuilder.append("用户：").append(dto.getContent()).append("\n");
        promptBuilder.append("助手：");

        String prompt = promptBuilder.toString();
        log.info("prompt:\n{}", prompt);

        StringBuilder fullReply = new StringBuilder();

        // 6. 调AI（流式）
        aiService.streamChat(prompt, new AIService.StreamCallback() {

            @Override
            public void onMessage(String text) {
                try {
                    fullReply.append(text);
                    emitter.send(text);
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete() {
                try {
                    emitter.complete();

                    // 7. 存AI完整回复
                    ChatMessage aiMsg = new ChatMessage();
                    aiMsg.setSessionId(dto.getSessionId());
                    aiMsg.setRole("assistant");
                    aiMsg.setContent(fullReply.toString());
                    chatMessageMapper.insert(aiMsg);

                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            }
        });
    }

    private String buildRagContext(Long userId, String question) {

        // 1. embedding
        List<Float> queryVec = embeddingService.embedding(question);

        // 2. 扩大召回（关键：从5改成50）
        List<Long> ids = faissService.search(queryVec, 50);

        if (ids == null || ids.isEmpty()) {
            return "无相关日程";
        }

        // 3. 按 userId 过滤（关键）
        List<Schedule> schedules =
                scheduleService.listByUserIdAndIds(userId, ids);

        if (schedules == null || schedules.isEmpty()) {
            return "无相关日程";
        }

        // 4. 截断（只取前5条）
        schedules = schedules.stream()
                .limit(5)
                .toList();

        // 5. 拼 context
        StringBuilder context = new StringBuilder();
        for (Schedule s : schedules) {
            context.append(s.getName())
                    .append(" ")
                    .append(s.getStartTime())
                    .append("\n");
        }

        return context.toString();
    }


    @Override
    public List<ChatMessage> listMessage(Long sessionId) {
        return chatMessageMapper.listBySessionId(sessionId);
    }
}