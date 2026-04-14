package com.example.aihelper.server.service.impl;

import com.example.aihelper.common.constant.MessageConstant;
import com.example.aihelper.common.context.UserContext;
import com.example.aihelper.common.exception.NotLoginException;
import com.example.aihelper.common.exception.TimeErrorException;
import com.example.aihelper.pojo.dto.ScheduleCreateDTO;
import com.example.aihelper.pojo.dto.ScheduleUpdateDTO;
import com.example.aihelper.pojo.entity.Schedule;
import com.example.aihelper.server.mapper.ScheduleMapper;
import com.example.aihelper.server.service.EmbeddingService;
import com.example.aihelper.server.service.FaissService;
import com.example.aihelper.server.service.ScheduleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private EmbeddingService embeddingService;
    @Autowired
    private FaissService faissService;
    @Override
    public void createSchedule(ScheduleCreateDTO dto, Long userId) {
        if (userId == null) {
            throw new NotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(dto, schedule);

        if (schedule.getStartTime() == null) {
            throw new TimeErrorException(MessageConstant.TIME_NULL_ERROR);
        }

        if (schedule.getEndTime() != null &&
                schedule.getStartTime().isAfter(schedule.getEndTime())) {
            throw new TimeErrorException(MessageConstant.TIME_ERROR);
        }

        schedule.setUserId(userId);
        schedule.setCreateTime(LocalDateTime.now());
        schedule.setUpdateTime(LocalDateTime.now());

        scheduleMapper.insert(schedule);

        // ✅ 新增直接 embedding
        embedding(schedule);
    }

    @Override
    public void updateSchedule(ScheduleUpdateDTO dto, Long userId) {
        if (userId == null) {
            throw new RuntimeException(MessageConstant.USER_NOT_LOGIN);
        }

        Schedule old = scheduleMapper.selectById(dto.getId());
        if (old == null) {
            throw new RuntimeException("日程不存在");
        }

        if (!old.getUserId().equals(userId)) {
            throw new RuntimeException(MessageConstant.NO_RIGHT);
        }

        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(dto, schedule);

        if (schedule.getStartTime() == null) {
            throw new TimeErrorException(MessageConstant.TIME_NULL_ERROR);
        }

        if (schedule.getEndTime() != null &&
                schedule.getStartTime().isAfter(schedule.getEndTime())) {
            throw new TimeErrorException(MessageConstant.TIME_ERROR);
        }

        schedule.setUserId(userId);
        schedule.setUpdateTime(LocalDateTime.now());

        scheduleMapper.update(schedule);

        // ✅ 关键：先删旧向量，再加新向量
        faissService.remove(schedule.getId());
        embedding(schedule);
    }

    @Override
    public void deleteSchedule(Long id) {

        // ✅ 删除数据库
        scheduleMapper.delete(id);

        // ✅ 同步删除向量（必须）
        faissService.remove(id);
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

    public void embedding(Schedule schedule) {
        try {
            if (schedule.getId() != null) {
                faissService.remove(schedule.getId());
            }

            // 👉 2. 构造语义文本
            String content = buildContent(schedule);

            // 👉 3. embedding
            List<Float> vec = embeddingService.embedding(content);

            // 👉 4. 重新加入
            faissService.add(vec, schedule.getId());

        } catch (Exception e) {
            System.err.println("RAG处理失败");
            e.printStackTrace();
        }
    }

    private String buildContent(Schedule schedule) {

        String start = schedule.getStartTime().toString();
        String end = schedule.getEndTime() != null
                ? schedule.getEndTime().toString()
                : "未结束";

        return String.format(
                "用户在%s到%s有一个日程，名称是%s，备注是%s",
                start,
                end,
                schedule.getName(),
                schedule.getRemark() == null ? "无" : schedule.getRemark()
        );
    }

    @Override
    public List<Schedule> listByUserId() {
        Long userId = UserContext.getUserId();
        return scheduleMapper.listByUserId(userId);
    }

    @Override
    public List<Schedule> listByUserIdAndIds(Long userId, List<Long> ids) {
        if (userId == null || ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return scheduleMapper.listByUserIdAndIds(userId, ids);
    }
}