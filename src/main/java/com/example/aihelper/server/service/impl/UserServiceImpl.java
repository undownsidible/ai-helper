package com.example.aihelper.server.service.impl;

import com.example.aihelper.common.constant.MessageConstant;
import com.example.aihelper.common.exception.PasswordErrorException;
import com.example.aihelper.common.exception.AccountNotFoundException;
import com.example.aihelper.pojo.dto.UserLoginDTO;
import com.example.aihelper.pojo.dto.UserRegisterDTO;
import com.example.aihelper.pojo.entity.User;
import com.example.aihelper.server.mapper.UserMapper;
import com.example.aihelper.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void register(UserRegisterDTO dto) {

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(user);
    }

    @Override
    public User login(UserLoginDTO dto) {

        User user = userMapper.findByUsername(dto.getUsername());

        if(user == null){
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        if(!user.getPassword().equals(dto.getPassword())){
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        return user;
    }

    @Override
    public User getById(long userId) {
        return null;
    }
}