package com.gym.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Swagger 文档聚合 Header 过滤器
 * 将 /api/{service-id}/v2/api-docs 的请求路径重写为 /v2/api-docs
 */
@Component
public class SwaggerHeaderFilter implements GlobalFilter, Ordered {

    private static final String API_DOCS_SUFFIX = "/v2/api-docs";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (!path.endsWith(API_DOCS_SUFFIX)) {
            return chain.filter(exchange);
        }

        // path like /api/auth/v2/api-docs -> /v2/api-docs
        ServerHttpRequest newReq = request.mutate().path(API_DOCS_SUFFIX).build();
        return chain.filter(exchange.mutate().request(newReq).build());
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
