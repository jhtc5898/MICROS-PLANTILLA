package org.example.accountservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateDto {
    
    // Se pueden actualizar account_type, status e initial_balance
    @JsonProperty("account_type")
    private String accountType;
    
    @JsonProperty("status")
    private Boolean status;
    
    @JsonProperty("initial_balance")
    private BigDecimal initialBalance;
}

