package com.playerslog.backend.shorturl.domain;

import com.playerslog.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "short_url")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrl extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(nullable = false)
    private Long gollId;

    // Optional: for future expiration logic
    private LocalDateTime expiresAt;
}
