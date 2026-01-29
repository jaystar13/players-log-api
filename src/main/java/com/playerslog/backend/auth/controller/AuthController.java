package com.playerslog.backend.auth.controller;

import com.playerslog.backend.auth.dto.AccessTokenResponse;
import com.playerslog.backend.auth.service.AuthService;
import com.playerslog.backend.global.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.playerslog.backend.global.auth.OAuth2SuccessHandler.REFRESH_TOKEN_COOKIE_NAME;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<AccessTokenResponse> reissue(HttpServletRequest request) {
        String refreshToken = CookieUtils.getCookie(request, REFRESH_TOKEN_COOKIE_NAME)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token cookie not found"))
                .getValue();

        String accessToken = authService.reissueAccessToken(refreshToken);

        return ResponseEntity.ok(new AccessTokenResponse(accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.getCookie(request, REFRESH_TOKEN_COOKIE_NAME)
                .ifPresent(cookie -> {
                    authService.logout(cookie.getValue());
                    CookieUtils.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
                });

        return ResponseEntity.noContent().build();
    }
}
