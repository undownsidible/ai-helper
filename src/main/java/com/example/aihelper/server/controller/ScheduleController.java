package com.example.aihelper.server.controller;

import com.example.aihelper.common.result.Result;
import com.example.aihelper.pojo.dto.ScheduleCreateDTO;
import com.example.aihelper.pojo.dto.ScheduleUpdateDTO;
import com.example.aihelper.pojo.entity.Schedule;
import com.example.aihelper.server.service.ScheduleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/schedule")
@Api(tags = "日程相关接口")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 创建日程
     */
    @PostMapping
    @ApiOperation("创建日程")
    public Result create(@RequestBody ScheduleCreateDTO dto) {

        scheduleService.createSchedule(dto);

        return Result.success();
    }

    /**
     * 查询用户全部日程
     */
    @ApiOperation("查询用户全部日程")
    @GetMapping("/list")
    public Result list(Long userId) {

        List<Schedule> list = scheduleService.listByUserId(userId);

        return Result.success(list);
    }

    /**
     * 查询今日日程
     */
    @ApiOperation("查询今日日程")
    @GetMapping("/today")
    public Result today(Long userId) {

        List<Schedule> list = scheduleService.getTodaySchedule(userId);

        return Result.success(list);
    }

    /**
     * 更新日程
     */
    @ApiOperation("更新日程")
    @PutMapping
    public Result update(@RequestBody ScheduleUpdateDTO dto) {

        scheduleService.updateSchedule(dto);

        return Result.success();
    }

    /**
     * 删除日程（软删除）
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除日程")
    public Result delete(@PathVariable Long id) {

        scheduleService.deleteSchedule(id);

        return Result.success();
    }
}