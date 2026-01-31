package com.playerslog.backend.auth.domain;

import com.playerslog.backend.auth.security.oauth2.AppleOAuth2UserInfo;
import com.playerslog.backend.auth.security.oauth2.FacebookOAuth2UserInfo;
import com.playerslog.backend.auth.security.oauth2.GoogleOAuth2UserInfo;
import com.playerslog.backend.auth.security.oauth2.OAuth2UserInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum AuthProvider {
    GOOGLE("google", GoogleOAuth2UserInfo::new),
    APPLE("apple", AppleOAuth2UserInfo::new),
    FACEBOOK("facebook", FacebookOAuth2UserInfo::new);

    private final String registrationId;

    private final Function<Map<String, Object>, OAuth2UserInfo> factory;

    public static AuthProvider fromRegistrationId(String id) {
        return Arrays.stream(values())
                .filter(p -> p.registrationId.equalsIgnoreCase(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 소셜 미디어입니다."));
    }

    public OAuth2UserInfo createUserInfo(Map<String, Object> attributes) {
        return factory.apply(attributes);
    }

}
