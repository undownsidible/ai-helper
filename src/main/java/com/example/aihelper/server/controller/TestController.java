package com.example.aihelper.server.controller;

import com.example.aihelper.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api("测试接口")
@RequestMapping("/test")
public class TestController {
    @ApiOperation("测试")
    @GetMapping("/testOperation")
    public Result testOperation(){
        return Result.success();
    }
}
