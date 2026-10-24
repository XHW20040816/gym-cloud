package com.gym.gateway.filter;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.fastjson2.JSON;
import com.gym.common.result.R;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Sentinel 限流自定义降级处理器
 * Gateway 限流时返回统一 JSON 格式（HTTP 429）
 */
public class SentinelFallbackHandler implements BlockRequestHandler {

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
        return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(JSON.toJSONString(R.fail(429, "Blocked by Sentinel: 请求过于频繁")));
    }
}
