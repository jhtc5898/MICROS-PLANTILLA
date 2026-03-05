package org.example.accountservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovementDto {
    
    private Long id;
    
    @NotNull(message = "Account ID is required")
    @JsonProperty("account_id")
    private Long accountId;
    
    @JsonProperty("movement_date")
    private LocalDateTime movementDate;
    
    @NotBlank(message = "Movement type is required")
    @JsonProperty("movement_type")
    private String movementType; // DEBIT or CREDIT
    
    @NotNull(message = "Value is required")
    @Positive(message = "Value must be greater than zero")
    @JsonProperty("value")
    private BigDecimal value;
    
    @JsonProperty("balance")
    private BigDecimal balance;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}

