package com.example.aihelper.server.service.impl;

import com.example.aihelper.common.exception.TimeErrorException;
import com.example.aihelper.pojo.dto.ScheduleCreateDTO;
import com.example.aihelper.pojo.dto.ScheduleUpdateDTO;
import com.example.aihelper.pojo.entity.Schedule;
import com.example.aihelper.server.mapper.ScheduleMapper;
import com.example.aihelper.server.service.ScheduleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    public void createSchedule(ScheduleCreateDTO dto) {

        //将dto复制给实体
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(dto, schedule);

        if(schedule.getStartTime() == null){
            throw new TimeErrorException("开始时间不能为空");
        }

        if (schedule.getEndTime() != null &&
                schedule.getStartTime().isAfter(schedule.getEndTime())) {
            throw new TimeErrorException("开始时间不能晚于结束时间");
        }

        schedule.setDeleted(0);
        schedule.setCreateTime(LocalDateTime.now());
        schedule.setUpdateTime(LocalDateTime.now());

        scheduleMapper.insert(schedule);
    }

    @Override
    public List<Schedule> listByUserId(Long userId) {
        return scheduleMapper.listByUserId(userId);
    }

    @Override
    public void updateSchedule(ScheduleUpdateDTO dto) {

        // 1. DTO 转 Entity
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(dto, schedule);

        // 2. 更新时间
        schedule.setUpdateTime(LocalDateTime.now());

        // 3. 执行更新
        scheduleMapper.update(schedule);
    }

    @Override
    public void deleteSchedule(Long id) {
        scheduleMapper.delete(id);
    }

    // 查询今天日程
    @Override
    public List<Schedule> getTodaySchedule(Long userId) {

        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return scheduleMapper.getTodaySchedule(userId, start, end);
    }

    // 判断时间冲突
    @Override
    public boolean checkScheduleConflict(Long userId, Schedule schedule) {

        List<Schedule> list = scheduleMapper.checkScheduleConflict(
                userId,
                schedule.getStartTime(),
                schedule.getEndTime()
        );

        return !list.isEmpty();
    }
}