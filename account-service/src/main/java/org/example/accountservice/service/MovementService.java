package org.example.accountservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.accountservice.dto.MovementDto;
import org.example.accountservice.entity.Account;
import org.example.accountservice.entity.Movement;
import org.example.accountservice.mapper.MovementMapper;
import org.example.accountservice.repository.AccountRepository;
import org.example.accountservice.repository.MovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovementService {
    
    private final MovementRepository movementRepository;
    private final AccountRepository accountRepository;
    private final MovementMapper movementMapper;
    
    private static final String DEBIT = "DEBIT";
    private static final String CREDIT = "CREDIT";
    
    @Transactional
    public Mono<MovementDto> createMovement(MovementDto movementDto) {
        log.info("Creating movement of type: {} with value: {} for account: {}", 
                movementDto.getMovementType(), movementDto.getValue(), movementDto.getAccountId());
        
        if (movementDto.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Movement value must be greater than zero"));
        }
        
        return accountRepository.findById(movementDto.getAccountId())
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found with ID: " + movementDto.getAccountId())))
                .flatMap(account -> {
                    if (!account.getStatus()) {
                        return Mono.error(new RuntimeException("Account is inactive"));
                    }
                    
                    BigDecimal newBalance;
                    if (DEBIT.equalsIgnoreCase(movementDto.getMovementType())) {
                        newBalance = account.getCurrentBalance().subtract(movementDto.getValue());
                        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                            log.warn("Insufficient balance for account: {}. Current: {}, Requested: {}", 
                                    account.getId(), account.getCurrentBalance(), movementDto.getValue());
                            return Mono.error(new RuntimeException("Saldo no disponible"));
                        }
                    } else if (CREDIT.equalsIgnoreCase(movementDto.getMovementType())) {
                        newBalance = account.getCurrentBalance().add(movementDto.getValue());
                    } else {
                        return Mono.error(new IllegalArgumentException("Invalid movement type. Must be DEBIT or CREDIT"));
                    }
                    
                    account.setCurrentBalance(newBalance);
                    account.setUpdatedAt(LocalDateTime.now());
                    
                    Movement movement = movementMapper.toEntity(movementDto);
                    movement.setMovementDate(LocalDateTime.now());
                    movement.setBalance(newBalance);
                    movement.setCreatedAt(LocalDateTime.now());
                    
                    return accountRepository.save(account)
                            .then(movementRepository.save(movement));
                })
                .map(movementMapper::toDto)
                .doOnSuccess(m -> log.info("Movement created successfully with ID: {}", m.getId()))
                .doOnError(error -> log.error("Error creating movement: {}", error.getMessage()));
    }
    
    public Mono<MovementDto> getMovementById(Long id) {
        log.info("Fetching movement with ID: {}", id);
        return movementRepository.findById(id)
                .map(movementMapper::toDto)
                .switchIfEmpty(Mono.error(new RuntimeException("Movement not found with ID: " + id)))
                .doOnError(error -> log.error("Error fetching movement: {}", error.getMessage()));
    }
    
    public Flux<MovementDto> getAllMovements() {
        log.info("Fetching all movements");
        return movementRepository.findAll()
                .map(movementMapper::toDto)
                .doOnError(error -> log.error("Error fetching movements: {}", error.getMessage()));
    }
    
    public Flux<MovementDto> getMovementsByAccountId(Long accountId) {
        log.info("Fetching movements for account ID: {}", accountId);
        return movementRepository.findByAccountId(accountId)
                .map(movementMapper::toDto)
                .doOnError(error -> log.error("Error fetching movements: {}", error.getMessage()));
    }
    
    @Transactional
    public Mono<MovementDto> updateMovement(Long id, MovementDto movementDto) {
        log.info("Updating movement with ID: {}", id);
        return movementRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Movement not found with ID: " + id)))
                .flatMap(existing -> {
                    movementMapper.updateEntityFromDto(movementDto, existing);
                    return movementRepository.save(existing);
                })
                .map(movementMapper::toDto)
                .doOnSuccess(m -> log.info("Movement updated successfully with ID: {}", m.getId()))
                .doOnError(error -> log.error("Error updating movement: {}", error.getMessage()));
    }
    
    @Transactional
    public Mono<Void> deleteMovement(Long id) {
        log.info("Deleting movement with ID: {}", id);
        return movementRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Movement not found with ID: " + id)))
                .flatMap(movementRepository::delete)
                .doOnSuccess(v -> log.info("Movement deleted successfully with ID: {}", id))
                .doOnError(error -> log.error("Error deleting movement: {}", error.getMessage()));
    }
}

