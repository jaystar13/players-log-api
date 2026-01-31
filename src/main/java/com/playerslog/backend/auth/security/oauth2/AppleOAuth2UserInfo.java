package com.playerslog.backend.auth.security.oauth2;

import com.playerslog.backend.auth.domain.AuthProvider;

import java.util.Map;

public class AppleOAuth2UserInfo extends OAuth2UserInfo {

    public AppleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        // Apple은 처음 인증시에만 name을 제공
        Object name = attributes.get("name");
        if (name instanceof Map) {
            Map<String, Object> nameMap = (Map<String, Object>) name;
            String firstName = (String) nameMap.get("firstName");
            String lastName = (String) nameMap.get("lastName");
            return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        }
        return (String) attributes.get("email"); // fallback
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        // Apple은 프로필 이미지를 제공하지 않음
        return null;
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.APPLE;
    }
}
