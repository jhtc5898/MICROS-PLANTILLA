package org.example.accountservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.accountservice.dto.AccountDto;
import org.example.accountservice.dto.AccountUpdateDto;
import org.example.accountservice.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing bank accounts.
 * Provides endpoints for creating, reading, updating, and deleting accounts.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Controller", description = "API for managing accounts")
public class AccountController {
    
    private final AccountService accountService;
    
    /** Creates a new account. */
    @PostMapping
    @Operation(summary = "Create a new account", description = "Creates a new account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public Mono<ResponseEntity<AccountDto>> createAccount(@Valid @RequestBody AccountDto accountDto) {
        log.info("POST /api/v1/accounts - Creating account");
        return accountService.createAccount(accountDto)
                .map(account -> ResponseEntity.status(HttpStatus.CREATED).body(account));
    }
    
    /** Retrieves an account by its ID. */
    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID", description = "Retrieves an account by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public Mono<ResponseEntity<AccountDto>> getAccountById(
            @Parameter(description = "Account ID") @PathVariable Long id) {
        log.info("GET /api/v1/accounts/{} - Fetching account", id);
        return accountService.getAccountById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    /** Retrieves all accounts. */
    @GetMapping
    @Operation(summary = "Get all accounts", description = "Retrieves all accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully")
    })
    public Mono<ResponseEntity<Flux<AccountDto>>> getAllAccounts() {
        log.info("GET /api/v1/accounts - Fetching all accounts");
        return Mono.just(ResponseEntity.ok(accountService.getAllAccounts()));
    }
    
    /** Retrieves all accounts for a specific customer. */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get accounts by customer ID", description = "Retrieves all accounts for a specific customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully")
    })
    public Mono<ResponseEntity<Flux<AccountDto>>> getAccountsByCustomerId(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        log.info("GET /api/v1/accounts/customer/{} - Fetching accounts for customer", customerId);
        return Mono.just(ResponseEntity.ok(accountService.getAccountsByCustomerId(customerId)));
    }
    
    /** Updates an existing account. */
    @PutMapping("/{id}")
    @Operation(summary = "Update account", description = "Updates an existing account. Only ID is required. Only account_type, status and initial_balance can be updated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public Mono<ResponseEntity<AccountDto>> updateAccount(
            @Parameter(description = "Account ID") @PathVariable Long id,
            @RequestBody AccountUpdateDto updateDto) {
        log.info("PUT /api/v1/accounts/{} - Updating account", id);
        return accountService.updateAccount(id, updateDto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    /** Deletes an account by its ID. */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete account", description = "Deletes an account by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public Mono<ResponseEntity<Void>> deleteAccount(
            @Parameter(description = "Account ID") @PathVariable Long id) {
        log.info("DELETE /api/v1/accounts/{} - Deleting account", id);
        return accountService.deleteAccount(id)
                .then(Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).<Void>build()))
                .onErrorResume(error -> {
                    if (error.getMessage() != null && error.getMessage().contains("not found")) {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).<Void>build());
                    }
                    return Mono.error(error);
                });
    }
}

