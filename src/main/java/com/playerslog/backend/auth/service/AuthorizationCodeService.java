package com.playerslog.backend.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthorizationCodeService {

    private static final long CODE_EXPIRATION_SECONDS = 60;
    private static final String CODE_PREFIX = "oauth2_code:";
    private final StringRedisTemplate redisTemplate;

    public String generateAndStoreCode(String accessToken) {
        String code = UUID.randomUUID().toString();
        String key = CODE_PREFIX + code;
        redisTemplate.opsForValue().set(key, accessToken, CODE_EXPIRATION_SECONDS, TimeUnit.SECONDS);
        return code;
    }

    public String getAccessTokenForCode(String code) {
        String key = CODE_PREFIX + code;
        String accessToken = redisTemplate.opsForValue().get(key);
        if (accessToken != null) {
            redisTemplate.delete(key); // Use the code only once
        }
        return accessToken;
    }
}
