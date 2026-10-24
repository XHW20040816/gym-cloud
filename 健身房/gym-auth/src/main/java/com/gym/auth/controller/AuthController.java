package com.gym.auth.controller;

import com.gym.api.dto.LoginUserDTO;
import com.gym.api.dto.TokenVO;
import com.gym.api.dto.UserInfoVO;
import com.gym.api.user.UserFeignClient;
import com.gym.common.constant.RedisConstants;
import com.gym.common.exception.BizException;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.common.util.JwtUtil;
import com.gym.common.util.RedisCache;
import com.gym.entity.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Api(tags = "认证接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private RedisCache redisCache;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.ttl.access:7200}")
    private Integer accessTtl;

    @Value("${jwt.ttl.refresh:604800}")
    private Long refreshTtl;

    private SecretKey getSignKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 手动构建 JWT（支持自定义过期时间，用于生成 refresh token）
     */
    private String buildToken(Map<String, Object> claims, long ttlSeconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ttlSeconds * 1000);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @ApiOperation("登录：账号密码获取 access_token + refresh_token")
    @PostMapping("/login")
    public R<TokenVO> login(@RequestBody LoginUserDTO dto) {
        // 1. AuthenticationManager 进行用户名/密码认证（内部走 UserDetailsService → Feign）
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        // 2. 拿到认证后的 username，再次通过 Feign 获取用户完整信息（id、role 等）
        String username = authentication.getName();
        R<User> userResult = userFeignClient.getUserByUsername(username);
        if (userResult == null || !userResult.isSuccess() || userResult.getData() == null) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "用户不存在");
        }
        User user = userResult.getData();
        Long userId = user.getId();
        String role = user.getRole() != null ? user.getRole().name() : "USER";

        // 3. 生成 claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("username", username);

        // 4. 生成 access_token（TTL = accessTtl = 7200s）
        String accessToken = buildToken(claims, accessTtl);

        // 5. 生成 refresh_token（TTL = refreshTtl = 604800s = 7d）
        String refreshToken = buildToken(claims, refreshTtl);

        // 6. 存入 Redis
        // access token
        String accessKey = RedisConstants.TOKEN_KEY + "access:" + accessToken;
        redisCache.set(accessKey, userId, accessTtl, TimeUnit.SECONDS);
        // refresh token
        String refreshKey = RedisConstants.TOKEN_KEY + "refresh:" + refreshToken;
        redisCache.set(refreshKey, userId, refreshTtl, TimeUnit.SECONDS);

        // 7. 返回 TokenVO
        TokenVO vo = TokenVO.builder()
                .access_token(accessToken)
                .refresh_token(refreshToken)
                .token_type("Bearer")
                .expires_in(accessTtl)
                .build();
        return R.ok(vo);
    }

    @ApiOperation("刷新 access_token：Header 传入 refresh_token")
    @PostMapping("/refresh")
    public R<TokenVO> refresh(HttpServletRequest request) {
        // 1. 从 Header 获取 refresh_token（兼容 Bearer 或 直接值）
        String authHeader = request.getHeader("refresh_token");
        if (!StringUtils.hasText(authHeader)) {
            authHeader = request.getHeader("Authorization");
        }
        if (!StringUtils.hasText(authHeader)) {
            return R.fail(ResultCode.UNAUTHORIZED, "缺少 refresh_token");
        }
        String refreshToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        // 2. Redis 校验 refresh_token 是否存在
        String refreshKey = RedisConstants.TOKEN_KEY + "refresh:" + refreshToken;
        Object obj = redisCache.get(refreshKey);
        if (obj == null) {
            return R.fail(ResultCode.UNAUTHORIZED, "refresh_token 已失效或不存在");
        }

        // 3. 解析 refresh_token，获取 claims
        Claims claims;
        try {
            claims = jwtUtil.parseToken(refreshToken);
        } catch (Exception e) {
            return R.fail(ResultCode.UNAUTHORIZED, "refresh_token 无效");
        }
        Long userId = jwtUtil.getUserId(claims);
        String role = jwtUtil.getRole(claims);
        String username = jwtUtil.getUsername(claims);

        // 4. 生成新的 access_token（保留原 refresh_token）
        Map<String, Object> newClaims = new HashMap<>();
        newClaims.put("userId", userId);
        newClaims.put("role", role);
        newClaims.put("username", username);
        String newAccessToken = buildToken(newClaims, accessTtl);

        // 5. 新 access_token 存入 Redis
        String accessKey = RedisConstants.TOKEN_KEY + "access:" + newAccessToken;
        redisCache.set(accessKey, userId, accessTtl, TimeUnit.SECONDS);

        // 6. 返回：保留原 refresh_token（这里返回原值，客户端继续使用）
        TokenVO vo = TokenVO.builder()
                .access_token(newAccessToken)
                .refresh_token(refreshToken)
                .token_type("Bearer")
                .expires_in(accessTtl)
                .build();
        return R.ok(vo);
    }

    @ApiOperation("注销：清除 access + refresh 两个 Redis key")
    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        // 1. 从 Authorization 头拿到 access token
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            String accessKey = RedisConstants.TOKEN_KEY + "access:" + accessToken;
            redisCache.delete(accessKey);

            // 2. 根据 access token 解析 claims，拿到用户标识后匹配 refresh token（简单起见：从 Header 里也拿 refresh_token 删除）
            try {
                Claims claims = jwtUtil.parseToken(accessToken);
                Long userId = jwtUtil.getUserId(claims);
                // 扫描并删除该用户所有的 refresh_token（可选，下面是根据 Header 删除更直接）
                String refreshHeader = request.getHeader("refresh_token");
                if (StringUtils.hasText(refreshHeader)) {
                    String refreshToken = refreshHeader.startsWith("Bearer ") ? refreshHeader.substring(7) : refreshHeader;
                    String refreshKey = RedisConstants.TOKEN_KEY + "refresh:" + refreshToken;
                    redisCache.delete(refreshKey);
                }
            } catch (Exception ignored) {
            }
        }
        // 清空 SecurityContext
        SecurityContextHolder.clearContext();
        return R.ok();
    }

    @ApiOperation("获取当前登录用户信息")
    @GetMapping("/info")
    public R<UserInfoVO> info() {
        // 从 SecurityContext 中拿 username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText((String) authentication.getPrincipal())) {
            return R.fail(ResultCode.UNAUTHORIZED);
        }
        String username = (String) authentication.getPrincipal();

        // Feign 调用用户服务拿完整信息
        R<User> result = userFeignClient.getUserByUsername(username);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return R.fail(ResultCode.NOT_FOUND, "用户信息不存在");
        }
        User user = result.getData();

        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole() != null ? user.getRole().name() : null);
        vo.setAvatar(user.getAvatar());
        return R.ok(vo);
    }
}
