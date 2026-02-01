package com.playerslog.backend.goll.repository;

import com.playerslog.backend.goll.domain.Goll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GollRepository extends JpaRepository<Goll, Long> {
    
    @Query(value = "SELECT DISTINCT g FROM Goll g " +
                   "LEFT JOIN FETCH g.owner " +
                   "LEFT JOIN FETCH g.participants " +
                   "LEFT JOIN FETCH g.previewLinks",
           countQuery = "SELECT COUNT(g) FROM Goll g")
    Page<Goll> findAllWithDetails(Pageable pageable);

    @Query("SELECT g FROM Goll g " +
           "LEFT JOIN FETCH g.owner " +
           "LEFT JOIN FETCH g.participants p " +
           "LEFT JOIN FETCH g.previewLinks " +
           "WHERE g.id = :gollId")
    Optional<Goll> findByIdWithDetails(Long gollId);

    long countByOwnerId(Long ownerId);
}
