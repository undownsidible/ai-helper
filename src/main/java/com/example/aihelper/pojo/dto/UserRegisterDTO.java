package com.example.aihelper.pojo.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data

@ApiModel(description = "用户注册数据格式")
public class UserRegisterDTO {

    //用户名
    @NotBlank(message = "用户名不能为空")
    @ApiModelProperty(value = "用户名", required = true, example = "admin")
    private String username;
    //密码


    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "密码", required = true, example = "admin")
    private String password;
}