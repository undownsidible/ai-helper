package com.example.aihelper.server.service;
import com.example.aihelper.pojo.dto.UserLoginDTO;
import com.example.aihelper.pojo.dto.UserRegisterDTO;
import com.example.aihelper.pojo.entity.User;

public interface UserService {

    void register(UserRegisterDTO userRegisterDTO);
    User login(UserLoginDTO userLoginDTO);
    User getById(long userId);
}