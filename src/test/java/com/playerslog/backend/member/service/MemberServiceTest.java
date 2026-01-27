package com.playerslog.backend.member.service;

import com.playerslog.backend.global.auth.userinfo.Oauth2UserInfo;
import com.playerslog.backend.member.entity.Member;
import com.playerslog.backend.member.entity.Role;
import com.playerslog.backend.member.entity.SocialProvider;
import com.playerslog.backend.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 테스트")
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    private Oauth2UserInfo oauth2UserInfo;

    @BeforeEach
    void setUp() {
        // Mock 객체만 생성하고, when() 스터빙은 각 테스트 메소드에서 개별적으로 정의
        oauth2UserInfo = mock(Oauth2UserInfo.class);
    }

    @Test
    @DisplayName("신규 사용자일 경우, 회원가입을 진행하고 Member 객체를 반환한다")
    void processOAuth2User_whenUserIsNew_shouldCreateAndReturnMember() {
        // given
        // 이 테스트는 신규 회원 생성을 위해 모든 userInfo 정보가 필요
        when(oauth2UserInfo.getEmail()).thenReturn("test@example.com");
        when(oauth2UserInfo.getProviderId()).thenReturn("123456789");
        when(oauth2UserInfo.getNickname()).thenReturn("testUser");
        when(oauth2UserInfo.getProfileImageUrl()).thenReturn("http://image.url");

        when(memberRepository.findByProviderAndProviderId(SocialProvider.GOOGLE, "123456789"))
                .thenReturn(Optional.empty());
        when(memberRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Member result = memberService.processOAuth2User(SocialProvider.GOOGLE, oauth2UserInfo);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getProvider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(result.getProviderId()).isEqualTo("123456789");
        assertThat(result.getRole()).isEqualTo(Role.USER);

        // verify
        verify(memberRepository, times(1)).findByProviderAndProviderId(any(), any());
        verify(memberRepository, times(1)).findByEmail(any());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("기존에 소셜 로그인한 사용자일 경우, DB에서 조회하여 Member 객체를 반환한다")
    void processOAuth2User_whenSocialUserExists_shouldFindAndReturnMember() {
        // given
        // 이 테스트는 providerId로 사용자를 찾으므로 getProviderId 스터빙만 필요
        when(oauth2UserInfo.getProviderId()).thenReturn("123456789");

        Member existingMember = Member.builder()
                .email("test@example.com")
                .provider(SocialProvider.GOOGLE)
                .providerId("123456789")
                .nickname("testUser")
                .build();

        when(memberRepository.findByProviderAndProviderId(SocialProvider.GOOGLE, "123456789"))
                .thenReturn(Optional.of(existingMember));

        // when
        Member result = memberService.processOAuth2User(SocialProvider.GOOGLE, oauth2UserInfo);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(existingMember);

        // verify
        verify(memberRepository, times(1)).findByProviderAndProviderId(any(), any());
        verify(memberRepository, never()).findByEmail(any());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("이메일은 같지만 다른 소셜 계정으로 가입한 사용자의 경우, 기존 계정에 소셜 정보를 연동한다")
    void processOAuth2User_whenEmailExists_shouldLinkSocialAccount() {
        // given
        // 이 테스트는 providerId로 찾고, email로 다시 찾으므로 두 스터빙이 필요
        when(oauth2UserInfo.getEmail()).thenReturn("test@example.com");
        when(oauth2UserInfo.getProviderId()).thenReturn("123456789");

        Member existingMember = Member.builder()
                .email("test@example.com")
                .provider(SocialProvider.APPLE)
                .providerId("987654321")
                .nickname("testUser")
                .build();

        when(memberRepository.findByProviderAndProviderId(SocialProvider.GOOGLE, "123456789"))
                .thenReturn(Optional.empty());
        when(memberRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingMember));

        // when
        Member result = memberService.processOAuth2User(SocialProvider.GOOGLE, oauth2UserInfo);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProvider()).isEqualTo(SocialProvider.GOOGLE); // Provider가 GOOGLE로 변경되었는지 확인
        assertThat(result.getProviderId()).isEqualTo("123456789");       // ProviderId가 GOOGLE의 ID로 변경되었는지 확인

        // verify
        verify(memberRepository, times(1)).findByProviderAndProviderId(any(), any());
        verify(memberRepository, times(1)).findByEmail(any());
        verify(memberRepository, never()).save(any(Member.class)); // save는 호출되지 않음 (JPA의 dirty checking으로 업데이트)
    }
}
