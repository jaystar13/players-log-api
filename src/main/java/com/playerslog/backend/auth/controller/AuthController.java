package com.playerslog.backend.auth.controller;

import com.playerslog.backend.auth.domain.User;
import com.playerslog.backend.auth.dto.AuthResponse;
import com.playerslog.backend.auth.dto.TokenExchangeRequest;
import com.playerslog.backend.auth.service.AuthService;
import com.playerslog.backend.auth.service.AuthorizationCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthorizationCodeService authorizationCodeService;

    @PostMapping("/token/exchange")
    public ResponseEntity<AuthResponse> exchangeToken(@RequestBody TokenExchangeRequest request) {
        if (request == null || !StringUtils.hasText(request.getCode())) {
            return ResponseEntity.badRequest().build();
        }

        String accessToken = authorizationCodeService.getAccessTokenForCode(request.getCode());

        if (accessToken == null) {
            // Code is invalid, expired, or already used
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .build();

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.refreshAccessToken(request, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long userId,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        authService.logout(userId, request, response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserInfo> getCurrentUser(@AuthenticationPrincipal Long userId) {
        User user = authService.getCurrentUser(userId);

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .provider(user.getProvider().name())
                .build();

        return ResponseEntity.ok(userInfo);
    }
}
