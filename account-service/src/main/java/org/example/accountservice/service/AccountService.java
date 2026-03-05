package org.example.accountservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.accountservice.dto.AccountDto;
import org.example.accountservice.dto.AccountUpdateDto;
import org.example.accountservice.entity.Account;
import org.example.accountservice.mapper.AccountMapper;
import org.example.accountservice.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    
    @Transactional
    public Mono<AccountDto> createAccount(AccountDto accountDto) {
        log.info("Creating account with number: {} for customer: {}", 
                accountDto.getAccountNumber(), accountDto.getCustomerId());
        
        return accountRepository.findByAccountNumber(accountDto.getAccountNumber())
                .flatMap(existing -> Mono.error(new RuntimeException(
                    String.format("Ya existe una cuenta registrada con el número '%s'. Por favor, utilice un número de cuenta diferente.", 
                    accountDto.getAccountNumber()))))
                .cast(Account.class)
                .switchIfEmpty(
                        Mono.defer(() -> {
                            Account account = accountMapper.toEntity(accountDto);
                            account.setCurrentBalance(account.getInitialBalance());
                            account.setCreatedAt(LocalDateTime.now());
                            account.setUpdatedAt(LocalDateTime.now());
                            return accountRepository.save(account);
                        })
                )
                .map(accountMapper::toDto)
                .doOnSuccess(a -> log.info("Account created successfully with ID: {}", a.getId()))
                .doOnError(error -> log.error("Error creating account: {}", error.getMessage()));
    }
    
    public Mono<AccountDto> getAccountById(Long id) {
        log.info("Fetching account with ID: {}", id);
        return accountRepository.findById(id)
                .map(accountMapper::toDto)
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found with ID: " + id)))
                .doOnError(error -> log.error("Error fetching account: {}", error.getMessage()));
    }
    
    public Flux<AccountDto> getAllAccounts() {
        log.info("Fetching all accounts");
        return accountRepository.findAll()
                .map(accountMapper::toDto)
                .doOnError(error -> log.error("Error fetching accounts: {}", error.getMessage()));
    }
    
    public Flux<AccountDto> getAccountsByCustomerId(Long customerId) {
        log.info("Fetching accounts for customer ID: {}", customerId);
        return accountRepository.findByCustomerId(customerId)
                .map(accountMapper::toDto)
                .doOnError(error -> log.error("Error fetching accounts: {}", error.getMessage()));
    }
    
    @Transactional
    public Mono<AccountDto> updateAccount(Long id, AccountUpdateDto updateDto) {
        log.info("Updating account with ID: {}", id);
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found with ID: " + id)))
                .flatMap(existing -> {
                    if (updateDto.getAccountType() != null) {
                        existing.setAccountType(updateDto.getAccountType());
                    }
                    if (updateDto.getStatus() != null) {
                        existing.setStatus(updateDto.getStatus());
                    }
                    if (updateDto.getInitialBalance() != null) {
                        existing.setInitialBalance(updateDto.getInitialBalance());
                    }
                    existing.setUpdatedAt(LocalDateTime.now());
                    return accountRepository.save(existing);
                })
                .map(accountMapper::toDto)
                .doOnSuccess(a -> log.info("Account updated successfully with ID: {}", a.getId()))
                .doOnError(error -> log.error("Error updating account: {}", error.getMessage()));
    }
    
    @Transactional
    public Mono<Void> deleteAccount(Long id) {
        log.info("Deleting account with ID: {}", id);
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found with ID: " + id)))
                .flatMap(accountRepository::delete)
                .doOnSuccess(v -> log.info("Account deleted successfully with ID: {}", id))
                .doOnError(error -> log.error("Error deleting account: {}", error.getMessage()));
    }
    
    public Mono<Account> getAccountEntityById(Long id) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found with ID: " + id)));
    }
}

