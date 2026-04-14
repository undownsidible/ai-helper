package com.example.aihelper.server.service;

import com.example.aihelper.pojo.dto.ScheduleCreateDTO;
import com.example.aihelper.pojo.dto.ScheduleUpdateDTO;
import com.example.aihelper.pojo.entity.Schedule;

import java.util.List;

public interface ScheduleService {

    void createSchedule(ScheduleCreateDTO dto, Long userId);

    List<Schedule> listByUserId();

    List<Schedule> listByUserIdAndIds(Long userId, List<Long> ids);

    void updateSchedule(ScheduleUpdateDTO dto, Long userId);

    void deleteSchedule(Long id);

    // 查询今天的日程
    List<Schedule> getTodaySchedule(Long userId);

    // 检测日程冲突
    boolean checkScheduleConflict(Long userId, Schedule schedule);
}