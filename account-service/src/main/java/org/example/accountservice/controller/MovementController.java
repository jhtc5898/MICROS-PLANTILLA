package org.example.accountservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.accountservice.dto.MovementDto;
import org.example.accountservice.service.MovementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing bank movements.
 * Provides endpoints for creating, reading, updating, and deleting movements.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/movements")
@RequiredArgsConstructor
@Tag(name = "Movement Controller", description = "API for managing movements")
public class MovementController {
    
    private final MovementService movementService;
    
    /** Creates a new movement. */
    @PostMapping
    @Operation(summary = "Create a new movement", description = "Creates a new movement (DEBIT or CREDIT)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Movement created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or insufficient balance")
    })
    public Mono<ResponseEntity<MovementDto>> createMovement(@Valid @RequestBody MovementDto movementDto) {
        log.info("POST /api/v1/movements - Creating movement");
        return movementService.createMovement(movementDto)
                .map(movement -> ResponseEntity.status(HttpStatus.CREATED).body(movement))
                .onErrorResume(error -> {
                    if (error.getMessage() != null && error.getMessage().contains("Saldo no disponible")) {
                        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(MovementDto.builder().build()));
                    }
                    return Mono.error(error);
                });
    }
    
    /** Retrieves a movement by its ID. */
    @GetMapping("/{id}")
    @Operation(summary = "Get movement by ID", description = "Retrieves a movement by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movement found"),
            @ApiResponse(responseCode = "404", description = "Movement not found")
    })
    public Mono<ResponseEntity<MovementDto>> getMovementById(
            @Parameter(description = "Movement ID") @PathVariable Long id) {
        log.info("GET /api/v1/movements/{} - Fetching movement", id);
        return movementService.getMovementById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    /** Retrieves all movements. */
    @GetMapping
    @Operation(summary = "Get all movements", description = "Retrieves all movements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movements retrieved successfully")
    })
    public Mono<ResponseEntity<Flux<MovementDto>>> getAllMovements() {
        log.info("GET /api/v1/movements - Fetching all movements");
        return Mono.just(ResponseEntity.ok(movementService.getAllMovements()));
    }
    
    /** Retrieves all movements for a specific account. */
    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get movements by account ID", description = "Retrieves all movements for a specific account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movements retrieved successfully")
    })
    public Mono<ResponseEntity<Flux<MovementDto>>> getMovementsByAccountId(
            @Parameter(description = "Account ID") @PathVariable Long accountId) {
        log.info("GET /api/v1/movements/account/{} - Fetching movements for account", accountId);
        return Mono.just(ResponseEntity.ok(movementService.getMovementsByAccountId(accountId)));
    }
    
    /** Updates an existing movement. */
    @PutMapping("/{id}")
    @Operation(summary = "Update movement", description = "Updates an existing movement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movement updated successfully"),
            @ApiResponse(responseCode = "404", description = "Movement not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public Mono<ResponseEntity<MovementDto>> updateMovement(
            @Parameter(description = "Movement ID") @PathVariable Long id,
            @Valid @RequestBody MovementDto movementDto) {
        log.info("PUT /api/v1/movements/{} - Updating movement", id);
        return movementService.updateMovement(id, movementDto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    /** Deletes a movement by its ID. */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete movement", description = "Deletes a movement by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Movement deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Movement not found")
    })
    public Mono<ResponseEntity<Void>> deleteMovement(
            @Parameter(description = "Movement ID") @PathVariable Long id) {
        log.info("DELETE /api/v1/movements/{} - Deleting movement", id);
        return movementService.deleteMovement(id)
                .then(Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).<Void>build()))
                .onErrorResume(error -> {
                    if (error.getMessage() != null && error.getMessage().contains("not found")) {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).<Void>build());
                    }
                    return Mono.error(error);
                });
    }
}

