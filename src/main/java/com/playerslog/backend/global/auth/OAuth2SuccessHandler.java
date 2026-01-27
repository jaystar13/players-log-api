package com.playerslog.backend.global.auth;

import com.playerslog.backend.global.auth.userinfo.Oauth2UserInfo;
import com.playerslog.backend.global.config.properties.Oauth2Properties;
import com.playerslog.backend.member.entity.Member;
import com.playerslog.backend.member.entity.SocialProvider;
import com.playerslog.backend.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final Oauth2Properties oauth2Properties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        var oAuthToken = (OAuth2AuthenticationToken) authentication;
        String regId = oAuthToken.getAuthorizedClientRegistrationId();
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        SocialProvider provider = SocialProvider.fromRegistrationId(regId);
        Oauth2UserInfo userInfo = provider.createUserInfo(oAuth2User.getAttributes());

        Member member = memberService.processOAuth2User(provider, userInfo);

        String token = jwtProvider.createToken(member.getId(), member.getRole().name());

        String targetUrl = UriComponentsBuilder.fromUriString(oauth2Properties.getSuccessRedirectUri())
                .fragment("token=" + token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
