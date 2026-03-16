package com.example.aihelper.pojo.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import java.time.LocalDateTime;


@ApiModel("用户")
@Data
public class User {

    private Long id;

    private String username;

    private String password;

    private LocalDateTime createTime;
}