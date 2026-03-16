package com.example.aihelper.server.service.impl;

import com.example.aihelper.common.constant.MessageConstant;
import com.example.aihelper.common.exception.SessionNotFoundException;
import com.example.aihelper.pojo.dto.ChatSendDTO;
import com.example.aihelper.pojo.entity.ChatMessage;
import com.example.aihelper.server.mapper.ChatMessageMapper;
import com.example.aihelper.server.mapper.SessionMapper;
import com.example.aihelper.server.service.AIService;
import com.example.aihelper.server.service.ChatService;
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

    // 历史消息限制
    private static final int MAX_HISTORY = 10;

    @Override
    public String send(ChatSendDTO dto) {

        if (sessionMapper.getById(dto.getSessionId()) == null) {
            throw new SessionNotFoundException(MessageConstant.SESSION_NOT_FOUND);
        }

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(dto.getSessionId());
        userMsg.setRole("user");
        userMsg.setContent(dto.getContent());

        chatMessageMapper.insert(userMsg);

        List<ChatMessage> history =
                chatMessageMapper.listRecentBySessionId(dto.getSessionId(), MAX_HISTORY);

        Collections.reverse(history);

        StringBuilder promptBuilder = new StringBuilder();

        int start = Math.max(0, history.size() - MAX_HISTORY);

        for (int i = start; i < history.size(); i++) {

            ChatMessage msg = history.get(i);

            if ("user".equals(msg.getRole())) {
                promptBuilder.append(MessageConstant.USER)
                        .append(msg.getContent())
                        .append("\n");
            } else {
                promptBuilder.append(MessageConstant.ASSISTANT)
                        .append(msg.getContent())
                        .append("\n");
            }
        }

        promptBuilder.append(MessageConstant.USER)
                .append(dto.getContent())
                .append("\n");

        promptBuilder.append(MessageConstant.ASSISTANT);

        String prompt = promptBuilder.toString();

        log.info("调用AI");

        String reply = aiService.chat(prompt);

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(dto.getSessionId());
        aiMsg.setRole("assistant");
        aiMsg.setContent(reply);

        chatMessageMapper.insert(aiMsg);

        return reply;
    }

    @Override
    public void streamChat(ChatSendDTO dto, SseEmitter emitter) {

        if (sessionMapper.getById(dto.getSessionId()) == null) {
            throw new SessionNotFoundException(MessageConstant.SESSION_NOT_FOUND);
        }

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(dto.getSessionId());
        userMsg.setRole("user");
        userMsg.setContent(dto.getContent());

        chatMessageMapper.insert(userMsg);

        List<ChatMessage> history =
                chatMessageMapper.listRecentBySessionId(dto.getSessionId(), MAX_HISTORY);

        Collections.reverse(history);

        StringBuilder promptBuilder = new StringBuilder();

        for (ChatMessage msg : history) {

            if ("user".equals(msg.getRole())) {
                promptBuilder.append(MessageConstant.USER)
                        .append(msg.getContent())
                        .append("\n");
            } else {
                promptBuilder.append(MessageConstant.ASSISTANT)
                        .append(msg.getContent())
                        .append("\n");
            }
        }

        promptBuilder.append(MessageConstant.USER)
                .append(dto.getContent())
                .append("\n");

        promptBuilder.append(MessageConstant.ASSISTANT);

        String prompt = promptBuilder.toString();

        StringBuilder fullReply = new StringBuilder();

        log.info("调用AI流式返回");

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

    @Override
    public List<ChatMessage> listMessage(Long sessionId) {
        return chatMessageMapper.listBySessionId(sessionId);
    }


}