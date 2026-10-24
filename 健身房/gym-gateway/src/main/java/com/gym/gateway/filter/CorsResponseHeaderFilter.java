package com.gym.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 去除 Vary Header，避免浏览器重复处理跨域响应头
 */
public class CorsResponseHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() {
        // 在 NettyWriteResponseFilter 之后执行，order = -1
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            // 响应提交后 headers 变为只读集合，直接 removeIf 会抛 UnsupportedOperationException
            try {
                if (headers.containsKey(HttpHeaders.VARY)) {
                    headers.remove(HttpHeaders.VARY);
                }
            } catch (UnsupportedOperationException ignored) {
                // 只读响应头：Vary 已随响应发出，跳过即可，不影响业务
            }
        }));
    }
}
