package com.example.aihelper.server.service.impl;

import com.example.aihelper.common.context.UserContext;
import com.example.aihelper.pojo.dto.ChatSendDTO;
import com.example.aihelper.pojo.dto.ScheduleCreateDTO;
import com.example.aihelper.pojo.dto.ScheduleUpdateDTO;
import com.example.aihelper.pojo.entity.ChatMessage;
import com.example.aihelper.pojo.entity.Schedule;
import com.example.aihelper.server.mapper.ChatMessageMapper;
import com.example.aihelper.server.mapper.SessionMapper;
import com.example.aihelper.server.service.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private SessionMapper sessionMapper;

    //@Qualifier("ollamaService")
    @Qualifier("apiService")
    @Autowired
    private AIService aiService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private FaissService faissService;

    @Autowired
    private ScheduleService scheduleService;

    private static final int MAX_HISTORY = 10;

    private String functionString = """
你是日程助手，支持以下操作：create、update、delete。
规则：
1. 识别到用户有明确操作意图时，返回纯JSON，否则正常回答。
2. 修改和删除必须在“相关日程”中查找最匹配的一条记录，并使用其id，禁止随意生成id。
示例：
创建：{"action":"create","name":"开会","startTime":"2026-04-15 10:00","endTime":"2026-04-15 11:00","remark":"项目会议"}
修改：{"action":"update","id":1,"name":"开会","startTime":"2026-04-15 14:00"}
删除：{"action":"delete","id":1}
""";

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
        promptBuilder.append(functionString);
        promptBuilder.append("相关日程：\n")
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
        aiMsg.setContent(handleAiReply(reply.toString(), userId));
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
        promptBuilder.append(functionString);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        promptBuilder.append("当前时间：").append(LocalDateTime.now().format(formatter)).append("\n");
        promptBuilder.append("相关日程：\n")
                .append(context)
                .append("\n");


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
                    aiMsg.setContent(handleAiReply(fullReply.toString(), userId));
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
            context.append("id:")
                    .append(s.getId())
                    .append("  name:")
                    .append(s.getName())
                    .append("  开始时间:")
                    .append(formatTime(s.getStartTime()))
                    .append("  结束时间:")
                    .append(formatTime(s.getEndTime()))
                    .append("  备注:")
                    .append(s.getRemark())
                    .append("\n");
        }

        return context.toString();
    }
    @Override
    public List<ChatMessage> listMessage(Long sessionId) {
        return chatMessageMapper.listBySessionId(sessionId);
    }
    private String handleAiReply(String reply, Long userId) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode json = mapper.readTree(reply);
            String action = json.get("action").asText();

            switch (action) {
                case "create":
                    ScheduleCreateDTO createDTO = new ScheduleCreateDTO();
                    createDTO.setName(json.get("name").asText());
                    createDTO.setStartTime(LocalDateTime.parse(json.get("startTime").asText().replace(" ", "T")));
                    createDTO.setEndTime(LocalDateTime.parse(json.get("endTime").asText().replace(" ", "T")));
                    createDTO.setRemark(json.has("remark") ? json.get("remark").asText() : null);

                    scheduleService.createSchedule(createDTO, userId);
                    return "日程创建成功";

                case "update":
                    ScheduleUpdateDTO updateDTO = new ScheduleUpdateDTO();
                    updateDTO.setId(json.get("id").asLong());
                    if (json.has("name")) {
                        updateDTO.setName(json.get("name").asText());
                    }
                    if (json.has("startTime")) {
                        updateDTO.setStartTime(LocalDateTime.parse(json.get("startTime").asText().replace(" ", "T")));
                    }
                    if (json.has("endTime")) {
                        updateDTO.setEndTime(LocalDateTime.parse(json.get("endTime").asText().replace(" ", "T")));
                    }
                    if (json.has("remark")) {
                        updateDTO.setRemark(json.get("remark").asText());
                    }

                    scheduleService.updateSchedule(updateDTO, userId);
                    return "日程更新成功";

                case "delete":
                    scheduleService.deleteSchedule(json.get("id").asLong());
                    return "日程删除成功";
            }

        } catch (Exception e) {
            return reply;
        }
        return reply;
    }
    private String formatTime(LocalDateTime time) {
        if (time == null) return "";
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
