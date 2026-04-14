package com.example.aihelper.pojo.entity;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

@ApiModel("日程")
@Data
public class Schedule {

    private Long id;
    private Long userId;

    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}