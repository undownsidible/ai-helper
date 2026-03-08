package com.example.aihelper.pojo.dto;

import lombok.Data;

@Data
public class ChatSendDTO {
    private Long userId;

    private Long sessionId;

    private String content;

}