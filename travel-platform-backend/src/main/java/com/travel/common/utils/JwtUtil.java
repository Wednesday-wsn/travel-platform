package com.travel.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 */
@Component
public class JwtUtil {

    @Value("${travel.jwt.secret}")
    private String secret;

    @Value("${travel.jwt.expire}")
    private Long expire;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token
     *
     * @param userId 用户/商家/管理员 ID
     * @param role   角色：user / merchant / admin
     */
    public String createToken(Long userId, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expire * 1000);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getKey())
                .compact();
    }

    /**
     * 当前时刻生成的 token 对应的过期时间戳（毫秒），供前端判断本地登录态是否过期
     */
    public long expireAtMillis() {
        return System.currentTimeMillis() + expire * 1000;
    }

    /**
     * 解析 token，非法或过期会抛出异常
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
