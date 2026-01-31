package com.playerslog.backend.member.entity;

import com.playerslog.backend.auth.domain.AuthProvider;
import com.playerslog.backend.auth.domain.Role;
import com.playerslog.backend.auth.domain.SocialLinks;
import com.playerslog.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "email", "nickname"})
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String email;

    @Column(nullable = false)
    private String nickname;

    private String profileImageUrl;

    private String description;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private SocialLinks socialLinks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Builder
    public Member(String email, String nickname, String profileImageUrl,
                  String description, AuthProvider provider, String providerId, Role role) {
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.description = description;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role == null ? Role.USER : role;
        this.status = MemberStatus.ACTIVE;
    }

    public void updateProfile(String nickname, String description, SocialLinks socialLinks) {
        this.nickname = nickname;
        this.description = description;
        this.socialLinks = socialLinks;
    }

    public void linkSocialAccount(AuthProvider provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
    }

    public void updateRole(Role role) {
        this.role = role;
    }
}
