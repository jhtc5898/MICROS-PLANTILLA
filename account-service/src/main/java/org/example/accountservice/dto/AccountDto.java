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
public class AccountDto {
    
    private Long id;
    
    @NotBlank(message = "Account number is required")
    @JsonProperty("account_number")
    private String accountNumber;
    
    @NotBlank(message = "Account type is required")
    @JsonProperty("account_type")
    private String accountType;
    
    @NotNull(message = "Initial balance is required")
    @Positive(message = "Initial balance must be positive")
    @JsonProperty("initial_balance")
    private BigDecimal initialBalance;
    
    @JsonProperty("current_balance")
    private BigDecimal currentBalance;
    
    @NotNull(message = "Status is required")
    @JsonProperty("status")
    private Boolean status;
    
    @NotNull(message = "Customer ID is required")
    @JsonProperty("customer_id")
    private Long customerId;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}

