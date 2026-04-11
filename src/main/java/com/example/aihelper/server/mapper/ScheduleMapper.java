package com.example.aihelper.server.mapper;

import com.example.aihelper.pojo.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ScheduleMapper {

    void insert(Schedule schedule);

    Schedule selectById(Long scheduleId);

    List<Schedule> listByUserId(Long userId);

    List<Schedule> listByIds(@Param("ids") List<Long> ids);

    void update(Schedule schedule);

    void delete(Long id);

    List<Schedule> getTodaySchedule(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Schedule> checkScheduleConflict(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}