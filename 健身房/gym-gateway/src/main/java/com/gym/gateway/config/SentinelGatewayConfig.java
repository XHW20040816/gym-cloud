package com.gym.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.gym.gateway.filter.SentinelFallbackHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Sentinel Gateway 限流配置
 */
@Configuration
public class SentinelGatewayConfig {

    /**
     * 自定义限流处理器 Bean。
     * 必须是 static 的 @Bean 方法：本类在 @PostConstruct 中还要调用它，
     * 非 static 会形成「配置类创建 → 本类 Bean 方法 → 配置类」的循环依赖，Spring Boot 2.7 默认禁止。
     */
    @Bean
    public static BlockRequestHandler sentinelBlockHandler() {
        return new SentinelFallbackHandler();
    }

    /**
     * 初始化 GatewayCallbackManager 的 BlockHandler
     */
    @PostConstruct
    public void doInit() {
        GatewayCallbackManager.setBlockHandler(sentinelBlockHandler());
    }
}
