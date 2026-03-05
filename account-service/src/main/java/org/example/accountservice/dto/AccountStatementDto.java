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
public class AccountStatementDto {
    
    @JsonProperty("client_id")
    private Long clientId;
    
    @JsonProperty("accounts")
    private List<AccountStatementDetailDto> accounts;
}

