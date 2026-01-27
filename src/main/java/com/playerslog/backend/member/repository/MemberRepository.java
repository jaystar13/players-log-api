package com.playerslog.backend.member.repository;

import com.playerslog.backend.member.entity.Member;
import com.playerslog.backend.member.entity.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByProviderAndProviderId(SocialProvider provider, String providerId);
}
