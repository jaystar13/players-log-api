package com.playerslog.backend.goll.repository;

import com.playerslog.backend.goll.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
