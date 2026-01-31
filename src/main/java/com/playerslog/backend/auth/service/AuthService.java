package com.playerslog.backend.auth.service;

import com.playerslog.backend.auth.domain.User;
import com.playerslog.backend.auth.dto.AuthResponse;
import com.playerslog.backend.auth.repository.UserRepository;
import com.playerslog.backend.auth.security.jwt.JwtTokenProvider;
import com.playerslog.backend.auth.security.oauth2.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;

    @Transactional
    public AuthResponse refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 RefreshToken 추출
        String refreshTokenValue = cookieUtil.getCookie(request, CookieUtil.REFRESH_TOKEN_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        // RefreshToken 검증
        if (!tokenProvider.validateToken(refreshTokenValue)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // Redis에서 RefreshToken 조회
        Long userId = refreshTokenService.getUserIdByToken(refreshTokenValue);
        if (userId == null) {
            throw new IllegalArgumentException("Refresh token not found in storage");
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 새로운 AccessToken 발급
        String newAccessToken = tokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        // 새로운 RefreshToken 발급 (Refresh Token Rotation)
        String newRefreshToken = tokenProvider.createRefreshToken(user.getId());

        // 기존 RefreshToken 삭제 후 새로운 것 저장
        refreshTokenService.deleteRefreshTokenByToken(refreshTokenValue);
        refreshTokenService.saveRefreshToken(user.getId(), newRefreshToken);

        // HttpOnly 쿠키에 새로운 토큰 저장
        cookieUtil.addAccessTokenCookie(response, newAccessToken);
        cookieUtil.addRefreshTokenCookie(response, newRefreshToken);

        log.info("Refreshed tokens for user: {}", userId);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .profileImageUrl(user.getProfileImageUrl())
                        .provider(user.getProvider().name())
                        .build())
                .build();
    }

    @Transactional
    public void logout(Long userId, HttpServletRequest request, HttpServletResponse response) {
        // Redis에서 RefreshToken 삭제
        refreshTokenService.deleteRefreshToken(userId);

        // 쿠키 삭제
        cookieUtil.deleteAuthCookies(request, response);

        log.info("User logged out: {}", userId);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
