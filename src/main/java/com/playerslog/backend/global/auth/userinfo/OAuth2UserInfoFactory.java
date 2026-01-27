package com.playerslog.backend.global.auth.userinfo;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static Oauth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleUserInfo(attributes);
            case "apple" -> new AppleUserInfo(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다.");
        };
    }
}
