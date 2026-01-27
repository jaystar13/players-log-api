package com.playerslog.backend.support.security;

import com.playerslog.backend.member.entity.Member;
import com.playerslog.backend.member.entity.Role;
import com.playerslog.backend.member.entity.SocialProvider;
import java.lang.reflect.Field;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

@Component
@RequiredArgsConstructor
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Member member = Member.builder()
                .email(customUser.email())
                .nickname(customUser.nickname())
                .role(Role.valueOf(customUser.role()))
                .provider(SocialProvider.GOOGLE)
                .providerId("mock-provider-id-" + customUser.email())
                .build();

        try {
            Field idField = Member.class.getDeclaredField("id");
            idField.setAccessible(true);
            ReflectionUtils.setField(idField, member, customUser.id());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        var authentication = new UsernamePasswordAuthenticationToken(member, null, member.getRole().getAuthorities());
        context.setAuthentication(authentication);
        return context;
    }
}

