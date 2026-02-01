package com.playerslog.backend.auth.security.oauth2;

import com.playerslog.backend.auth.domain.Role;
import com.playerslog.backend.auth.security.UserPrincipal;
import com.playerslog.backend.user.domain.User;
import com.playerslog.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        User user = userRepository.findByEmail(oAuth2UserInfo.getEmail())
                .map(existingUser -> {
                    // 기존 사용자가 있으면, 새 소셜 로그인 정보로 업데이트 (계정 연결)
                    log.info("Existing user found with email {}. Linking new provider {}.", existingUser.getEmail(), oAuth2UserInfo.getProvider());
                    existingUser.linkSocialAccount(oAuth2UserInfo.getProvider(), oAuth2UserInfo.getId());
                    return existingUser;
                })
                .orElseGet(() -> registerNewUser(oAuth2UserInfo)); // 새 사용자인 경우 등록

        // 사용자 정보 업데이트 (이름, 프로필 이미지 변경 가능)
        user.updateProfile(oAuth2UserInfo.getName(), oAuth2UserInfo.getImageUrl());
        // userRepository.save(user)는 registerNewUser에서 처리되거나, 
        // 기존 유저인 경우 dirty-checking에 의해 트랜잭션 커밋 시 자동 업데이트 됩니다.

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "facebook" -> new FacebookOAuth2UserInfo(attributes);
            case "apple" -> new AppleOAuth2UserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        };
    }

    private User registerNewUser(OAuth2UserInfo oAuth2UserInfo) {
        User user = User.builder()
                .email(oAuth2UserInfo.getEmail())
                .name(oAuth2UserInfo.getName())
                .profileImageUrl(oAuth2UserInfo.getImageUrl())
                .provider(oAuth2UserInfo.getProvider())
                .providerId(oAuth2UserInfo.getId())
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }
}
