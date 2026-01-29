package com.playerslog.backend.auth.service;

import com.playerslog.backend.global.auth.JwtProvider;
import com.playerslog.backend.global.auth.RefreshToken;
import com.playerslog.backend.global.auth.RefreshTokenRepository;
import com.playerslog.backend.member.entity.Member;
import com.playerslog.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public String reissueAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findById(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        Member member = memberRepository.findById(refreshToken.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        return jwtProvider.createAccessToken(member.getId(), member.getRole().name());
    }

    public void logout(String refreshTokenValue) {
        refreshTokenRepository.deleteById(refreshTokenValue);
    }
}
