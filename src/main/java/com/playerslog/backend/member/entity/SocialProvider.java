package com.playerslog.backend.member.entity;

import com.playerslog.backend.global.auth.userinfo.AppleUserInfo;
import com.playerslog.backend.global.auth.userinfo.GoogleUserInfo;
import com.playerslog.backend.global.auth.userinfo.Oauth2UserInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum SocialProvider {
    GOOGLE("google", GoogleUserInfo::new),
    APPLE("apple", AppleUserInfo::new);

    private final String registrationId;

    private final Function<Map<String, Object>, Oauth2UserInfo> factory;

    public static SocialProvider fromRegistrationId(String id) {
        return Arrays.stream(values())
                .filter(p -> p.registrationId.equalsIgnoreCase(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 소셜 미디어입니다."));
    }

    public Oauth2UserInfo createUserInfo(Map<String, Object> attributes) {
        return factory.apply(attributes);
    }

}
