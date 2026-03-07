package com.example.aihelper.server.controller;

import com.example.aihelper.common.context.UserContext;
import com.example.aihelper.common.result.Result;
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
@RequestMapping("/session")
@Api(tags = "会话管理接口")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    /**
     * 创建会话
     */
    @PostMapping("/create")
    @ApiOperation("创建会话")
    public Result createSession() {


        log.info("创建会话线程id:{}", Thread.currentThread().getId());
        Long userId = UserContext.getUserId();

        Long sessionId = sessionService.createSession(userId);

        return Result.success(sessionId);
    }

    /**
     * 查询当前用户的会话列表
     */
    @GetMapping("/list")
    @ApiOperation("查询会话列表")
    public Result listSession() {

        Long userId = UserContext.getUserId();

        List<ChatSession> list = sessionService.listByUserId(userId);

        return Result.success(list);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/delete/{sessionId}")
    @ApiOperation("删除会话")
    public Result deleteSession(@PathVariable Long sessionId) {

        Long userId = UserContext.getUserId();

        sessionService.deleteSession(sessionId, userId);

        return Result.success();
    }
}