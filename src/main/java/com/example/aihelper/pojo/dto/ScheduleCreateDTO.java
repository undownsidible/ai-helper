package com.example.aihelper.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleCreateDTO {
    // 日程名称
    private String name;

    // 开始时间
    private LocalDateTime startTime;

    // 结束时间
    private LocalDateTime endTime;

    // 备注
    private String remark;

}