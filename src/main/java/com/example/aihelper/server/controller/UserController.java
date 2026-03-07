package com.example.aihelper.server.controller;

import com.auth0.jwt.JWT;
import com.example.aihelper.common.context.UserContext;
import com.example.aihelper.common.result.Result;
import com.example.aihelper.common.utils.JwtUtil;
import com.example.aihelper.pojo.dto.UserLoginDTO;
import com.example.aihelper.pojo.dto.UserRegisterDTO;
import com.example.aihelper.pojo.entity.User;
import com.example.aihelper.pojo.vo.UserLoginVO;
import com.example.aihelper.server.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/user")
@Api(tags = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result register(@Valid @RequestBody UserRegisterDTO dto) {

        userService.register(dto);

        return Result.success();
    }

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result login(@Valid @RequestBody UserLoginDTO dto){
        User user = userService.login(dto);

        String token = JwtUtil.createToken(user.getId());

        UserLoginVO vo = UserLoginVO.builder()
                .id(user.getId())
                .token(token)
                .build();
        return Result.success(vo);
    }

    @GetMapping("/info")
    @ApiOperation("用户信息")
    public Result userInfo(){

        Long userId = UserContext.getUserId();
        User user = userService.getById(userId);

        return Result.success(user);
    }
}