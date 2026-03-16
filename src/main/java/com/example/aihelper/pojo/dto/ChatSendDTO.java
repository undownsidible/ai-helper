package com.example.aihelper.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
@ApiModel(description = "发送聊天数据格式")
public class ChatSendDTO {
    @ApiModelProperty(value = "会话id", required = true, example = "1")
    private Long sessionId;

    @ApiModelProperty(value = "内容", required = true, example = "你好")
    private String content;

}