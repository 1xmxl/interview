package com.interview.utils;


import com.interview.constant.AuthRedisConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenHelper {
    private final StringRedisTemplate stringRedisTemplate;
    public void saveRefreshToken(String jwtId, Long userId, long expirationMillis)
    {
        String key= String.format(AuthRedisConstant.REDIS_LOGIN,jwtId);
        stringRedisTemplate.opsForValue().set(key,userId.toString(),expirationMillis, TimeUnit.SECONDS);
    }
    public boolean validateRefreshToken(String jwtId) {
        String key = String.format(AuthRedisConstant.REDIS_LOGIN,jwtId);
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
    public void deleteRefreshToken(String jwtId) {
            String key = String.format(AuthRedisConstant.REDIS_LOGIN,jwtId);
            stringRedisTemplate.delete(key);
        }

    // 根据jti获取关联的userId（确保是本人操作）
    public String getUserIdByJti(String jwtId) {
        String key = String.format(AuthRedisConstant.REDIS_LOGIN,jwtId);
        return stringRedisTemplate.opsForValue().get(key);
    }
    public void revokeRefreshToken(String jwtId) {
        String key = String.format(AuthRedisConstant.BLACKLIST_PREFIX,jwtId);
        stringRedisTemplate.delete(key);
    }
    public void blacklistAccessToken(String jti, long expirationSeconds) {
        String key = AuthRedisConstant.BLACKLIST_PREFIX + jti;
        // 存入黑名单，过期时间设为该Token的剩余有效期
        stringRedisTemplate.opsForValue().set(key, "true", expirationSeconds, TimeUnit.SECONDS);
    }

    /**
     * 校验Access Token是否在黑名单中
     */
    public boolean isTokenBlacklisted(String jti) {
        String key = AuthRedisConstant.BLACKLIST_PREFIX + jti;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
}
