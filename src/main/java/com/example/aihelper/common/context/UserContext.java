package com.example.aihelper.common.context;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserContext {

    private static final ThreadLocal<Long> USER_THREAD_LOCAL = new ThreadLocal<>();

    public static void setUserId(Long userId){

        USER_THREAD_LOCAL.set(userId);
    }

    public static Long getUserId(){
        return USER_THREAD_LOCAL.get();
    }

    public static void clear(){
        USER_THREAD_LOCAL.remove();
    }

}