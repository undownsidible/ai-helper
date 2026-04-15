package com.example.aihelper.server.config;

import com.example.aihelper.server.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns(
                        "/test/**",
                        "/sessions/**",
                        "/chat/**",
                        "/schedule/**"
                        )
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/doc.html",
                        "/swagger-ui/**",
                        "/v2/api-docs"
                );
    }
}