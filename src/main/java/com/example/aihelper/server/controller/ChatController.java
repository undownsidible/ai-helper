package com.example.aihelper.server.controller;

import com.example.aihelper.common.result.Result;
import com.example.aihelper.pojo.dto.ChatSendDTO;
import com.example.aihelper.server.service.ChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/chat")
@Api(tags = "聊天相关接口")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    @ApiOperation("发送消息")
    public Result send(@RequestBody ChatSendDTO dto){

        String reply = chatService.send(dto);

        return Result.success(reply);
    }
}
