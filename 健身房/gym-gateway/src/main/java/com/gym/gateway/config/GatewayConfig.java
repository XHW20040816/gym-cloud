package com.gym.gateway.config;

import com.gym.gateway.filter.CorsResponseHeaderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关配置。
 *
 * <p>跨域统一由 Nacos 中 gym-gateway-dev.yml 的 spring.cloud.gateway.globalcors 提供（唯一来源），
 * 这里不再声明 CorsWebFilter Bean，避免 Java 配置里的 "*" 覆盖 yml 中收敛后的 allowed-origin-patterns。
 */
@Configuration
public class GatewayConfig {

    /**
     * 去除 Vary Header，避免浏览器重复处理跨域响应
     */
    @Bean
    public CorsResponseHeaderFilter corsResponseHeaderFilter() {
        return new CorsResponseHeaderFilter();
    }
}
