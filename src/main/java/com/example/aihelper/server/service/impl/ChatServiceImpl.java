package com.example.aihelper.server.service.impl;

import com.example.aihelper.common.constant.MessageConstant;
import com.example.aihelper.common.exception.SessionNotFoundException;
import com.example.aihelper.pojo.dto.ChatSendDTO;
import com.example.aihelper.pojo.entity.ChatMessage;
import com.example.aihelper.server.mapper.ChatMessageMapper;
import com.example.aihelper.server.mapper.SessionMapper;
import com.example.aihelper.server.service.AIService;
import com.example.aihelper.server.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private AIService aiService;


    @Override
    public String send(ChatSendDTO dto) {
        //检查会话是否存在
        if (sessionMapper.getById(dto.getSessionId()) == null){
            throw new  SessionNotFoundException(MessageConstant.SESSION_NOT_FOUND);
        }

        //设置用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(dto.getSessionId());
        userMsg.setRole("user");
        userMsg.setContent(dto.getContent());

        //拼接历史消息
        List<ChatMessage> history = chatMessageMapper.listBySessionId(dto.getSessionId());

        StringBuilder promptBuilder = new StringBuilder();

        for (ChatMessage msg : history) {

            if ("user".equals(msg.getRole())) {
                promptBuilder.append(MessageConstant.USER).append(msg.getContent()).append("\n");
            } else {
                promptBuilder.append(MessageConstant.ASSISTANT).append(msg.getContent()).append("\n");
            }
        }

        promptBuilder.append(MessageConstant.USER).append(dto.getContent()).append("\n");
        promptBuilder.append(MessageConstant.ASSISTANT);

        String prompt = promptBuilder.toString();

        //保存用户消息
        chatMessageMapper.insert(userMsg);

        // 调用 AI
        String reply = aiService.chat(prompt);

        // 保存 AI 回复
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(dto.getSessionId());
        aiMsg.setRole("assistant");
        aiMsg.setContent(reply);

        chatMessageMapper.insert(aiMsg);

        return reply;
    }

    public void streamChat(ChatSendDTO dto, SseEmitter emitter) {
        //检查会话是否存在
        if (sessionMapper.getById(dto.getSessionId()) == null){
            throw new  SessionNotFoundException(MessageConstant.SESSION_NOT_FOUND);
        }
        new Thread(() -> {

            try {

                URL url = new URL("http://localhost:11434/api/generate");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                String body = """
            {
              "model":"qwen2",
              "prompt":"%s",
              "stream":true
            }
            """.formatted(dto.getContent());

                conn.getOutputStream().write(body.getBytes());

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(conn.getInputStream()));

                ObjectMapper mapper = new ObjectMapper();

                String line;
                StringBuilder fullReply = new StringBuilder();

                while ((line = reader.readLine()) != null) {

                    JsonNode json = mapper.readTree(line);

                    if (json.has("response")) {

                        String text = json.get("response").asText();

                        fullReply.append(text);

                        emitter.send(text);
                    }

                    if (json.has("done") && json.get("done").asBoolean()) {
                        break;
                    }
                }

                emitter.complete();
                //保存AI回复
                ChatMessage aiMsg = new ChatMessage();
                aiMsg.setSessionId(dto.getSessionId());
                aiMsg.setRole("assistant");
                aiMsg.setContent(fullReply.toString());

                chatMessageMapper.insert(aiMsg);

            } catch (Exception e) {
                emitter.completeWithError(e);
            }

        }).start();
    }
}