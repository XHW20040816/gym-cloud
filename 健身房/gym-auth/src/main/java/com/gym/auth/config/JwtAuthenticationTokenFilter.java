package com.gym.auth.config;

import com.gym.common.constant.RedisConstants;
import com.gym.common.util.JwtUtil;
import com.gym.common.util.RedisCache;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT 认证过滤器：从 Authorization: Bearer xxx 中提取 token，
 * 先到 Redis 验证 token 是否存在（logout 后就没了），
 * 再使用 JwtUtil 解析，最后构造 UsernamePasswordAuthenticationToken 写入 SecurityContext
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private RedisCache redisCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 获取 Authorization 头
        String authHeader = request.getHeader("Authorization");
        // 如果没有 token 或者不是 Bearer 类型，直接放行（后面的 Filter 会处理认证失败）
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            // 2. Redis 验证 token 是否存在（access token）
            String accessKey = RedisConstants.TOKEN_KEY + "access:" + token;
            String refreshKey = RedisConstants.TOKEN_KEY + "refresh:" + token;
            Object accessExist = redisCache.get(accessKey);
            Object refreshExist = redisCache.get(refreshKey);
            if (accessExist == null && refreshExist == null) {
                // token 在 Redis 中不存在（可能已 logout 或过期）
                filterChain.doFilter(request, response);
                return;
            }

            // 3. 解析 JWT token
            Claims claims = jwtUtil.parseToken(token);
            Long userId = jwtUtil.getUserId(claims);
            String username = jwtUtil.getUsername(claims);
            String role = jwtUtil.getRole(claims);

            // 4. 构造 authorities
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (StringUtils.hasText(role)) {
                authorities.add(new SimpleGrantedAuthority(role));
            }

            // 5. 生成 UsernamePasswordAuthenticationToken 写入 SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            // 附加细节：userId
            authentication.setDetails(userId);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // token 无效或解析失败，直接放行，交给后续认证入口处理
            logger.warn("JWT token 验证失败: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
