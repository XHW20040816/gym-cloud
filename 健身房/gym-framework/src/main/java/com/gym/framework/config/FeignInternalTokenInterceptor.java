package com.gym.framework.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Feign 调用拦截器：所有服务间 Feign 请求自动携带内部鉴权头 {@code X-Internal-Token}，
 * 与 {@link InternalApiInterceptor} 配合完成服务间内部接口鉴权。
 */
@Component
public class FeignInternalTokenInterceptor implements RequestInterceptor {

    @Value("${gym.internal.token:GymInternalToken2024}")
    private String internalToken;

    @Override
    public void apply(RequestTemplate template) {
        template.header("X-Internal-Token", internalToken);
    }
}
