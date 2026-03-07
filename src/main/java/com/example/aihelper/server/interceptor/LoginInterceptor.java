package com.example.aihelper.server.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.aihelper.common.constant.MessageConstant;
import com.example.aihelper.common.exception.NotLoginException;
import com.example.aihelper.common.exception.TokenError;
import com.example.aihelper.common.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String token = request.getHeader("token");

        if (token == null || token.isEmpty()) {
            throw new NotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        try {

            // 解析token
            DecodedJWT jwt = JWT.decode(token);

            // 获取userId
            Long userId = jwt.getClaim("userId").asLong();

            if (userId == null) {
                throw new TokenError(MessageConstant.TOKEN_ERROR);
            }

            // 保存到ThreadLocal
            log.info("线程id:{}", Thread.currentThread().getId());
            UserContext.setUserId(userId);
        } catch (Exception e) {
            throw new TokenError(MessageConstant.TOKEN_ERROR);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        // 清理ThreadLocal
        UserContext.clear();
    }
}