package com.gym.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * 注册内部接口拦截器。
 */
@Configuration
public class InternalApiConfig implements WebMvcConfigurer {

    @Resource
    private InternalApiInterceptor internalApiInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalApiInterceptor).addPathPatterns("/**");
    }
}
