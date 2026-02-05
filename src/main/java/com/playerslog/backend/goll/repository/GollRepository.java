package com.playerslog.backend.goll.repository;

import com.playerslog.backend.goll.domain.Goll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Query(value = "SELECT g FROM Goll g " +
            "LEFT JOIN FETCH g.owner " +
            "WHERE g.owner.id = :ownerId",
            countQuery = "SELECT COUNT(g) FROM Goll g WHERE g.owner.id = :ownerId")
    Page<Goll> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT g FROM Goll g LEFT JOIN FETCH g.owner WHERE g.id IN :ids")
    List<Goll> findByIdIn(Set<Long> ids);

    /**
     * Performs a full-text search on the title of the golls.
     * For this query to be efficient, a GIN index should be created on the title column in the database.
     * Example: CREATE INDEX idx_gin_goll_title ON goll USING GIN(to_tsvector('english', title));
     *
     * @param query    The search query.
     * @param pageable The pagination information.
     * @return A page of golls matching the search query.
     */
    @Query(value = "SELECT * FROM goll WHERE to_tsvector('english', title) @@ websearch_to_tsquery('english', :query) " +
            "ORDER BY ts_rank(to_tsvector('english', title), websearch_to_tsquery('english', :query)) DESC",
            countQuery = "SELECT count(*) FROM goll WHERE to_tsvector('english', title) @@ websearch_to_tsquery('english', :query)",
            nativeQuery = true)
    Page<Goll> searchByTitleFTS(String query, Pageable pageable);
}
