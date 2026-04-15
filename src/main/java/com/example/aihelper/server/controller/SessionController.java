package com.example.aihelper.server.controller;

import com.example.aihelper.common.context.UserContext;
import com.example.aihelper.common.result.Result;
import com.example.aihelper.pojo.dto.SessionUpdateDTO;
import com.example.aihelper.pojo.entity.ChatSession;
import com.example.aihelper.server.service.SessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sessions")  // RESTful：资源用复数
@Api(tags = "会话管理接口")
public class SessionController {
    @Autowired
    private SessionService sessionService;

    /**
     * 创建会话 → POST /sessions（标准REST）
     */
    @PostMapping
    @ApiOperation("创建会话")
    public Result<Long> createSession() {
        log.info("创建会话线程id:{}", Thread.currentThread().getId());
        Long userId = UserContext.getUserId();
        Long sessionId = sessionService.createSession(userId);
        return Result.success(sessionId);
    }

    /**
     * 查询会话列表 → GET /sessions（标准REST）
     */
    @GetMapping
    @ApiOperation("查询会话列表")
    public Result<List<ChatSession>> listSession() {
        Long userId = UserContext.getUserId();
        List<ChatSession> list = sessionService.listByUserId(userId);
        return Result.success(list);
    }

    /**
     * 修改会话名称/日程 → PUT /sessions/{sessionId}（标准REST）
     */
    @PutMapping()
    @ApiOperation("修改会话名称/日程")
    public Result updateSession(@RequestBody SessionUpdateDTO dto) {
        Long userId = UserContext.getUserId();
        sessionService.updateSessionName(dto, userId);
        return Result.success();
    }

    /**
     * 删除会话 → DELETE /sessions/{sessionId}（标准REST）
     */
    @DeleteMapping("/{sessionId}")
    @ApiOperation("删除会话")
    public Result deleteSession(@PathVariable Long sessionId) {
        Long userId = UserContext.getUserId();
        sessionService.deleteSession(sessionId, userId);
        return Result.success();
    }
}