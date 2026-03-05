package org.example.accountservice.repository;

import org.example.accountservice.entity.Account;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for Account entities.
 * Provides CRUD operations and custom queries for accounts.
 */
@Repository
public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {
    
    /** Finds an account by its account number. */
    Mono<Account> findByAccountNumber(String accountNumber);
    
    /** Finds all accounts for a specific customer. */
    Flux<Account> findByCustomerId(Long customerId);
    
    /** Finds all accounts for a specific customer with a given status. */
    Flux<Account> findByCustomerIdAndStatus(Long customerId, Boolean status);
}

