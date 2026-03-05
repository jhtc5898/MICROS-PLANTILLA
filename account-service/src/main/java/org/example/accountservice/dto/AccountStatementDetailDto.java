package org.example.accountservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatementDetailDto {
    
    @JsonProperty("account_number")
    private String accountNumber;
    
    @JsonProperty("account_type")
    private String accountType;
    
    @JsonProperty("current_balance")
    private BigDecimal currentBalance;
    
    @JsonProperty("movements")
    private List<MovementDto> movements;
}

