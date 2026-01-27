package com.playerslog.backend.member.service;

import com.playerslog.backend.global.auth.userinfo.Oauth2UserInfo;
import com.playerslog.backend.member.dto.MemberUpdateDto;
import com.playerslog.backend.member.entity.Member;
import com.playerslog.backend.member.entity.SocialProvider;
import com.playerslog.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member processOAuth2User(SocialProvider provider, Oauth2UserInfo userInfo) {
        // 1. 소셜 정보로 회원 조회
        Optional<Member> memberOptional = memberRepository.findByProviderAndProviderId(provider, userInfo.getProviderId());
        if (memberOptional.isPresent()) {
            return memberOptional.get();
        }

        // 2. 이메일로 회원 조회
        Optional<Member> memberByEmail = memberRepository.findByEmail(userInfo.getEmail());
        if (memberByEmail.isPresent()) {
            // 3. 기존 회원에 소셜 정보 연동
            Member existingMember = memberByEmail.get();
            existingMember.linkSocialAccount(provider, userInfo.getProviderId());
            return existingMember;
        }

        // 4. 신규 회원 생성
        return createMember(provider, userInfo);
    }

    private Member createMember(SocialProvider provider, Oauth2UserInfo userInfo) {
        Member member = Member.builder()
                .email(userInfo.getEmail())
                .nickname(userInfo.getNickname())
                .profileImageUrl(userInfo.getProfileImageUrl())
                .provider(provider)
                .providerId(userInfo.getProviderId())
                .build();
        return memberRepository.save(member);
    }

    public void updateMyProfile(Member member, MemberUpdateDto dto) {
        member.updateProfile(dto.nickname(), dto.description(), dto.socialLinks());
    }
}
