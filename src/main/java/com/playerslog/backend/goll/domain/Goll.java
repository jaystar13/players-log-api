package com.playerslog.backend.goll.domain;

import com.playerslog.backend.global.common.BaseTimeEntity;
import com.playerslog.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "goll")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Goll extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String sport;

    private LocalDateTime matchDate;

    private String venue;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String matchType; // "vs" or "multi"

    @Column(nullable = false)
    private String participantUnit; // "individual" or "team"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "goll", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Participant> participants = new HashSet<>();

    @BatchSize(size = 100)
    @ElementCollection
    @CollectionTable(name = "goll_preview_links", joinColumns = @JoinColumn(name = "goll_id"))
    @Column(name = "url")
    private Set<String> previewLinks = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GollStatus status = GollStatus.ACTIVE;

    public void setStatus(GollStatus status) {
        this.status = status;
    }


    @Builder
    public Goll(String title, String sport, LocalDateTime matchDate, String venue, String description,
                String matchType, String participantUnit, User owner) {
        this.title = title;
        this.sport = sport;
        this.matchDate = matchDate;
        this.venue = venue;
        this.description = description;
        this.matchType = matchType;
        this.participantUnit = participantUnit;
        this.owner = owner;
    }

    public void setParticipants(Set<Participant> participants) {
        this.participants.clear();
        if (participants != null) {
            this.participants.addAll(participants);
            participants.forEach(p -> p.setGoll(this));
        }
    }

    public void setPreviewLinks(Set<String> previewLinks) {
        this.previewLinks.clear();
        if (previewLinks != null) {
            this.previewLinks.addAll(previewLinks);
        }
    }

    public void update(String title, String sport, LocalDateTime matchDate, String venue, String description,
                       String matchType, String participantUnit) {
        this.title = title;
        this.sport = sport;
        this.matchDate = matchDate;
        this.venue = venue;
        this.description = description;
        this.matchType = matchType;
        this.participantUnit = participantUnit;
    }
}
