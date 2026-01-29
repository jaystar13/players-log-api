package com.playerslog.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories(basePackages = "com.playerslog.backend.global.auth")
public class RedisConfig {
}
