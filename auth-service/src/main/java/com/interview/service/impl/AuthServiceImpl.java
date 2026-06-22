package com.interview.service.impl;

import client.UserClient;
import com.interview.DTO.LoginResponse;
import com.interview.DTO.UserDTO;
import com.interview.DTO.UsersDTO;
import com.interview.constant.AuthRedisConstant;
import com.interview.service.AuthService;


import com.interview.utils.JwtUtil;
import com.interview.utils.RefreshTokenHelper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserClient userClient;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;
    private final RefreshTokenHelper refreshTokenHelper;
    @Override
    public LoginResponse login(String username, String password) {
        UsersDTO one = userClient.getUsers(username,password);
        String key = String.format(AuthRedisConstant.REDIS_LOGIN,one.getId());
        String accessToken  = jwtUtil.generateAccessToken(one.getId().toString(), username);
        String refreshToken = jwtUtil.generateRefreshToken(one.getId().toString(), username);
        String jwtId = jwtUtil.getJwtIdFromToken(refreshToken);
        refreshTokenHelper.saveRefreshToken(jwtId,one.getId(),jwtUtil.REFRESH_EXPIRATION);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setRefreshToken(refreshToken);
        loginResponse.setAccessToken(accessToken);
        loginResponse.setUsername(username);
        return loginResponse;
    }

    @Override
    public void register(UserDTO usersDTO) {
        String username = usersDTO.getUsername();
        String passwordHash = usersDTO.getPasswordHash();
        String hashpw = BCrypt.hashpw(passwordHash, BCrypt.gensalt());
        usersDTO.setPasswordHash(hashpw);
        userClient.saveNewUsers(usersDTO);
    }

    @Override
    public Map<String, String> refresh(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Refresh Token 缺失");
        }
        String refreshToken = authHeader.substring(7);
        boolean b = jwtUtil.validateToken(refreshToken);
        if (!b) {
            throw new IllegalArgumentException("refresh token invalid");
        }
        String jwtId = jwtUtil.getJwtIdFromToken(refreshToken);
        boolean b1 = refreshTokenHelper.validateRefreshToken(jwtId);
        if (!b1) {
            throw new IllegalArgumentException("refresh token invalid");
        }
        String key= String.format(AuthRedisConstant.REDIS_LOGIN,jwtId);
        refreshTokenHelper.deleteRefreshToken(key);
        String usernameFromToken = jwtUtil.getUsernameFromToken(refreshToken);
        String userIdFromToken = jwtUtil.getUserIdFromToken(refreshToken);
        String accessToken = jwtUtil.generateAccessToken(userIdFromToken, usernameFromToken);
        String newRefreshToken = jwtUtil.generateRefreshToken(userIdFromToken, usernameFromToken);
        String jwtIdFromToken = jwtUtil.getJwtIdFromToken(newRefreshToken);
        String newKey=String.format(AuthRedisConstant.REDIS_LOGIN,jwtIdFromToken);
        stringRedisTemplate.opsForValue().set(newKey,userIdFromToken, jwtUtil.REFRESH_EXPIRATION, TimeUnit.MILLISECONDS);
        Map<String, String> result = new HashMap<>();
        result.put("access_token", accessToken);
        result.put("refresh_token", newRefreshToken); // 轮换时返回新的
        return result;
    }

    @Override
    public Map<String, String> logout(String authHeader, String refreshTokenHeader) {
        if (refreshTokenHeader != null && refreshTokenHeader.startsWith("Bearer ")) {
            String refreshToken = refreshTokenHeader.substring(7);
            if (jwtUtil.validateToken(refreshToken)) {
                String jti = jwtUtil.getJwtIdFromToken(refreshToken);
                refreshTokenHelper.revokeRefreshToken(jti);
                log.info("Refresh Token已吊销: {}", jti);
            }
        }

        // 2. 处理Access Token（加入黑名单，实现立即失效）
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            if (jwtUtil.validateToken(accessToken)) {
                // 解析Token获取剩余有效期
                Claims claims = jwtUtil.explainJwtToken(accessToken);
                String jti = claims.getId();
                Date expiration = claims.getExpiration();
                long now = System.currentTimeMillis();
                long ttlSeconds = (expiration.getTime() - now) / 1000;

                if (ttlSeconds > 0) {
                    refreshTokenHelper.blacklistAccessToken(jti, ttlSeconds);
                    log.info("Access Token已加入黑名单: {}, 剩余有效期 {} 秒", jti, ttlSeconds);
                }
            }
        }

        // 3. 返回成功（前端收到后清除本地Token）
        return Map.of("code", "200", "message", "Logout success");
    }
}
