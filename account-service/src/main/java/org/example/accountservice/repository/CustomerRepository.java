package org.example.accountservice.repository;

import org.example.accountservice.entity.Customer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Repository for Customer entities.
 * Provides CRUD operations and custom queries for customers.
 */
@Repository
public interface CustomerRepository extends ReactiveCrudRepository<Customer, Long> {
    
    /** Finds a customer by its ID. */
    @NonNull
    Mono<Customer> findById(@NonNull Long id);
}
