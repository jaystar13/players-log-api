package com.playerslog.backend.goll.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "goll_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // "individual" or "team"

    @Column(nullable = false)
    private Integer displayOrder;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goll_id", nullable = false)
    @JsonBackReference
    private Goll goll;

    @Builder
    public Participant(String name, String type, Integer displayOrder) {
        this.name = name;
        this.type = type;
        this.displayOrder = displayOrder;
    }
}
