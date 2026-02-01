package com.playerslog.backend.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_REFRESH_TOKEN_PREFIX = "user_refresh_token:";
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;

    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(Long userId, String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String userKey = USER_REFRESH_TOKEN_PREFIX + userId;

        // 기존에 해당 유저에게 발급된 토큰이 있다면 삭제
        String existingToken = redisTemplate.opsForValue().get(userKey);
        if (existingToken != null) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + existingToken);
        }

        // 1. 유저 ID -> 리프레시 토큰 저장
        redisTemplate.opsForValue().set(
                userKey,
                refreshToken,
                REFRESH_TOKEN_VALIDITY_DAYS,
                TimeUnit.DAYS
        );

        // 2. 리프레시 토큰 -> 유저 ID 저장 (조회를 위한 역방향 데이터)
        redisTemplate.opsForValue().set(
                tokenKey,
                String.valueOf(userId),
                REFRESH_TOKEN_VALIDITY_DAYS,
                TimeUnit.DAYS
        );

        log.info("Saved refresh token for user: {}", userId);
    }

    public Long getUserIdByToken(String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String userId = redisTemplate.opsForValue().get(tokenKey);

        if (userId == null) {
            log.warn("Refresh token not found or expired: {}", refreshToken);
            return null;
        }

        return Long.parseLong(userId);
    }

    public boolean existsRefreshToken(String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        return redisTemplate.hasKey(tokenKey);
    }

    public void deleteRefreshToken(Long userId) {
        String userKey = USER_REFRESH_TOKEN_PREFIX + userId;
        String refreshToken = redisTemplate.opsForValue().get(userKey);

        if (refreshToken != null) {
            String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
            redisTemplate.delete(tokenKey);
            redisTemplate.delete(userKey);
            log.info("Deleted refresh token for user: {}", userId);
        }
    }

    public void deleteRefreshTokenByToken(String refreshToken) {
        Long userId = getUserIdByToken(refreshToken);

        if (userId != null) {
            String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
            String userKey = USER_REFRESH_TOKEN_PREFIX + userId;

            redisTemplate.delete(tokenKey);
            redisTemplate.delete(userKey);
            log.info("Deleted refresh token: {}", refreshToken);
        }
    }

    public void extendRefreshToken(String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        Long userId = getUserIdByToken(refreshToken);

        if (userId != null) {
            String userKey = USER_REFRESH_TOKEN_PREFIX + userId;

            redisTemplate.expire(tokenKey, REFRESH_TOKEN_VALIDITY_DAYS, TimeUnit.DAYS);
            redisTemplate.expire(userKey, REFRESH_TOKEN_VALIDITY_DAYS, TimeUnit.DAYS);

            log.info("Extended refresh token TTL for user: {}", userId);
        }
    }
}
