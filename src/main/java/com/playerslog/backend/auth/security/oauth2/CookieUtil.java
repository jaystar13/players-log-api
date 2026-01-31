package com.playerslog.backend.auth.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Base64;
import java.util.Optional;

@Component
public class CookieUtil {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @Value("${app.cookie.domain}")
    private String cookieDomain;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site}")
    private String cookieSameSite;

    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }

        return Optional.empty();
    }

    public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setMaxAge(maxAge);

        // 도메인 설정 (localhost가 아닐 때만)
        if (!"localhost".equals(cookieDomain)) {
            cookie.setDomain(cookieDomain);
        }

        // SameSite 설정을 위한 헤더 직접 추가
        String cookieHeader = String.format(
                "%s=%s; Path=/; Max-Age=%d; HttpOnly; %s SameSite=%s",
                name,
                value,
                maxAge,
                cookieSecure ? "Secure;" : "",
                cookieSameSite
        );

        if (!"localhost".equals(cookieDomain)) {
            cookieHeader += "; Domain=" + cookieDomain;
        }

        response.addHeader("Set-Cookie", cookieHeader);
    }

    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        addCookie(response, ACCESS_TOKEN_COOKIE_NAME, token, 3600); // 1시간
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        addCookie(response, REFRESH_TOKEN_COOKIE_NAME, token, 604800); // 7일
    }

    public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);

                    if (!"localhost".equals(cookieDomain)) {
                        cookie.setDomain(cookieDomain);
                    }

                    response.addCookie(cookie);
                }
            }
        }
    }

    public void deleteAuthCookies(HttpServletRequest request, HttpServletResponse response) {
        deleteCookie(request, response, ACCESS_TOKEN_COOKIE_NAME);
        deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
    }

    public String serialize(Object object) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(object));
    }

    public <T> T deserialize(Cookie cookie, Class<T> cls) {
        return cls.cast(
                SerializationUtils.deserialize(
                        Base64.getUrlDecoder().decode(cookie.getValue())
                )
        );
    }
}
