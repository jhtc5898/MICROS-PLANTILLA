package org.example.accountservice.repository;

import org.example.accountservice.entity.Movement;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

/**
 * Repository for Movement entities.
 * Provides CRUD operations and custom queries for movements.
 */
@Repository
public interface MovementRepository extends ReactiveCrudRepository<Movement, Long> {
    
    /** Finds all movements for a specific account. */
    Flux<Movement> findByAccountId(Long accountId);
    
    /** Finds movements for an account within a date range, ordered by date descending. */
    @Query("SELECT * FROM movements WHERE account_id = $1 AND movement_date >= $2 AND movement_date <= $3 ORDER BY movement_date DESC")
    Flux<Movement> findByAccountIdAndMovementDateBetween(Long accountId, LocalDateTime startDate, LocalDateTime endDate);
}

