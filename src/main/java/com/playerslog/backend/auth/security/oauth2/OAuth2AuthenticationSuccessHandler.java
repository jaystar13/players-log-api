package com.playerslog.backend.auth.security.oauth2;

import com.playerslog.backend.auth.security.UserPrincipal;
import com.playerslog.backend.auth.security.jwt.JwtTokenProvider;
import com.playerslog.backend.auth.service.AuthorizationCodeService;
import com.playerslog.backend.auth.service.RefreshTokenService;
import com.playerslog.backend.global.config.properties.AppProperties;
import com.playerslog.backend.user.domain.User;
import com.playerslog.backend.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final AppProperties appProperties;
    private final RefreshTokenService refreshTokenService;
    private final AuthorizationCodeService authorizationCodeService; // 의존성 추가
    private final UserRepository userRepository;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieRepository;
    private final CookieUtil cookieUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) {
        String redirectUri = cookieUtil.getCookie(
                        request,
                        HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME
                )
                .map(Cookie::getValue)
                .orElse(getDefaultTargetUrl());

        if (!isAuthorizedRedirectUri(redirectUri)) {
            throw new IllegalArgumentException("Unauthorized Redirect URI");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // 토큰 생성
        String accessToken = tokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        String refreshToken = tokenProvider.createRefreshToken(user.getId());

        // RefreshToken을 Redis에 저장하고 쿠키에 담기
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        // AccessToken에 대한 임시 코드 생성 및 Redis에 저장
        String code = authorizationCodeService.generateAndStoreCode(accessToken);

        log.info("OAuth2 login success: userId={}, provider={}", user.getId(), user.getProvider());

        // 리다이렉트 (임시 코드를 URL에 포함)
        return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("code", code) // 임시 코드를 URL 쿼리 파라미터로 추가
                .build().toUriString();
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);

        return appProperties.getOauth2().getAuthorizedRedirectUris()
                .stream()
                .anyMatch(authorizedRedirectUri -> {
                    URI authorizedURI = URI.create(authorizedRedirectUri);
                    return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorizedURI.getPort() == clientRedirectUri.getPort();
                });
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        cookieRepository.removeAuthorizationRequestCookies(request, response);
    }
}
