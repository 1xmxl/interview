package com.interview.utils;




import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.PostConstruct;
import lombok.Data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;


//
//@Component
@Data
@Slf4j
public class JwtUtil {
    @Value("${tj.secret}")
    private String jwtToken;
    public long ACCESS_EXPIRATION = 30 * 60 * 1000;      // 30分钟（短）
    public long REFRESH_EXPIRATION = 7 * 24 * 3600 * 1000; // 7天（长）
    private SecretKey secretKey ;
    @PostConstruct
    public void init() {
        this.secretKey = secretLock(jwtToken);
    }
    private String generateToken(String userId,String userName,Long expireTime)
    {
        String jwtId = UUID.randomUUID().toString();
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("userName",userName);
        map.put("jwtId",jwtId);
        String compact = Jwts.builder()
                .setClaims(map)
                .setId(jwtId)
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        return compact;
    }
    public String generateRefreshToken(String userId,String userName)
    {
        return generateToken(userId,userName,REFRESH_EXPIRATION);
    }
    public String generateAccessToken(String userId,String userName)
    {
        return generateToken(userId,userName,ACCESS_EXPIRATION);
    }
    public Claims explainJwtToken(String jwtToken)
    {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT token is empty");
        }
        return Jwts.parser()
                .verifyWith(secretKey)   // 设置验证密钥
                .build()
                .parseSignedClaims(jwtToken)  // 注意：新版用 parseSignedClaims
                .getPayload();                // getBody() → getPayload()
    }

        public boolean validateToken(String token) {
            try {
                Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token);  // 自动验证签名、过期等
                return true;
            } catch (JwtException | IllegalArgumentException e) {
                return false;
            }
        }
    public SecretKey secretLock(String jwtToken) {
        byte[] decode = Base64.getDecoder().decode(jwtToken);
        SecretKey secretKey = Keys.hmacShaKeyFor(decode);
        return secretKey;
    }
    public String getUserIdFromToken(String token) {
        Claims claims = explainJwtToken(token);
        String s = claims.get("userId", String.class);
        return s;
    }

    /**
     * 从JWT中提取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = explainJwtToken(token);
        String s = claims.get("userName", String.class);
        return s;
    }
    public String getJwtIdFromToken(String token) {
        Claims claims = explainJwtToken(token);
        String s = claims.get("jwtId", String.class);
        return s;
    }
}
