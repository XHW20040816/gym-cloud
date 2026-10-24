package com.gym.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 内部接口访问拦截器。
 *
 * <p>路径含 {@code /internal/} 的接口仅供服务间 Feign 调用，不允许被外部（绕过网关直连端口）
 * 访问。通过校验请求头 {@code X-Internal-Token} 与服务配置的内部令牌一致来放行，防止
 * 任意调用 internal 接口篡改余额/订单状态/库存等敏感数据。
 */
@Component
public class InternalApiInterceptor implements HandlerInterceptor {

    @Value("${gym.internal.token:GymInternalToken2024}")
    private String internalToken;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (path != null && path.contains("/internal/")) {
            String token = request.getHeader("X-Internal-Token");
            if (token == null || !internalToken.equals(token)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"msg\":\"禁止访问内部接口\",\"data\":null}");
                return false;
            }
        }
        return true;
    }
}
