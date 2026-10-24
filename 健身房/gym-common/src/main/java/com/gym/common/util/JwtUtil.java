package com.gym.common.util;

import com.gym.common.exception.BizException;
import com.gym.common.result.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire:7200}")
    private Long expire;

    private SecretKey getSignKey() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("jwt.secret 未配置，请检查 Nacos 配置中心");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Long userId, String role, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("username", username);
        return createToken(claims);
    }

    public String createToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expire * 1000);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "Token已过期");
        } catch (Exception e) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public Long getUserId(Claims claims) {
        Object userId = claims.get("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return getUserId(claims);
    }

    public String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }
}
