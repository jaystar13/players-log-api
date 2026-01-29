package com.playerslog.backend.global.auth;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@RedisHash(value = "refreshToken")
public class RefreshToken {

    @Id
    private String refreshToken;

    @Indexed
    private Long memberId;

    @TimeToLive
    private Long expiration;

    @Builder
    public RefreshToken(String refreshToken, Long memberId, Long expiration) {
        this.refreshToken = refreshToken;
        this.memberId = memberId;
        this.expiration = expiration / 1000; // 초 단위로 저장
    }
}
