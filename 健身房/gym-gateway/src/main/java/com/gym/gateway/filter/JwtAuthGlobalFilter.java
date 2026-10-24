package com.gym.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.gym.common.result.R;
import com.gym.gateway.config.AuthConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 全局JWT认证过滤器
 */
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthConfig authConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final String secret;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthGlobalFilter(AuthConfig authConfig,
                               StringRedisTemplate stringRedisTemplate,
                               @Value("${gym.jwt.secret}") String secret) {
        this.authConfig = authConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.secret = secret;
    }

    private SecretKey getSignKey() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("gym.jwt.secret 未配置，请检查 Nacos 配置中心");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // ① 拦截内部接口：/internal/ 仅供服务间 Feign 调用，不允许经网关访问
        if (path != null && path.contains("/internal/")) {
            return buildForbiddenResponse(exchange, "禁止访问内部接口");
        }

        // ② 白名单匹配放行
        List<String> whiteList = authConfig.getWhiteList();
        if (whiteList != null) {
            for (String pattern : whiteList) {
                if (pathMatcher.match(pattern, path)) {
                    return chain.filter(exchange);
                }
            }
        }

        // ③ 取 Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return buildUnauthorizedResponse(exchange, "未携带Token");
        }

        // ④ 处理 Bearer token
        String token;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            token = authHeader;
        }

        if (token.trim().isEmpty()) {
            return buildUnauthorizedResponse(exchange, "未携带Token");
        }

        // ⑤ 判断 Redis 是否存在 auth:token:access:{token}
        String redisKey = "auth:token:access:" + token;
        Boolean hasKey = stringRedisTemplate.hasKey(redisKey);
        if (hasKey == null || !hasKey) {
            return buildUnauthorizedResponse(exchange, "Token已失效或已注销");
        }

        // ⑥ 解析 JWT token
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return buildUnauthorizedResponse(exchange, "Token已过期");
        } catch (MalformedJwtException e) {
            return buildUnauthorizedResponse(exchange, "Token格式错误");
        } catch (Exception e) {
            return buildUnauthorizedResponse(exchange, "Token无效");
        }

        // ⑦ 取出用户信息
        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);
        String username = claims.get("username", String.class);

        if (userId == null) {
            return buildUnauthorizedResponse(exchange, "Token中用户信息缺失");
        }

        // ⑧ 把用户信息写入下游请求头
        String encodedUsername;
        try {
            encodedUsername = URLEncoder.encode(username, "UTF-8");
        } catch (Exception e) {
            encodedUsername = username;
        }

        ServerHttpRequest newRequest = request.mutate()
                .header("X-User-Id", userId.toString())
                .header("X-User-Role", role == null ? "" : role)
                .header("X-User-Name", encodedUsername)
                .build();

        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

        // ⑨ 放行
        return chain.filter(newExchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * 构建401未授权响应
     */
    private Mono<Void> buildUnauthorizedResponse(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = JSON.toJSONString(R.fail(401, msg));
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 构建403禁止访问响应（后续越权使用）
     */
    private Mono<Void> buildForbiddenResponse(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = JSON.toJSONString(R.fail(403, msg));
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
