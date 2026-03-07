package com.example.aihelper.server.mapper;

import com.example.aihelper.pojo.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 新建用户
     * @param user
     */
    @Insert("insert into user(username,password) values(#{username},#{password})")
    void insert(User user);

    /**
     * 查询用户
     */
    @Select("select * from user where username = #{username}")
    User findByUsername(String username);

    @Select("select * from user where id = #{id}")
    User findById(long id);
}